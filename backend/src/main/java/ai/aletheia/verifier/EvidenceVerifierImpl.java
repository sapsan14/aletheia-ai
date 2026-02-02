package ai.aletheia.verifier;

import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.tsp.TimeStampToken;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Offline verifier for Evidence Package (DP2.2.1).
 * Verification order: (1) hash, (2) signature, (3) TSA token.
 */
public class EvidenceVerifierImpl implements EvidenceVerifier {

    private static final String PROVIDER = "BC";
    private static final int SHA256_DIGEST_LENGTH = 32;
    private static final HexFormat HEX = HexFormat.of().withLowerCase();

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public VerificationResult verify(Path path) {
        List<String> report = new ArrayList<>();
        Path dir = path;
        boolean isZip = false;
        Path tempDir = null;

        try {
            if (Files.isRegularFile(path) && path.toString().toLowerCase(Locale.ROOT).endsWith(".aep")) {
                tempDir = Files.createTempDirectory("aep-");
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.isDirectory()) continue;
                        Path out = tempDir.resolve(entry.getName());
                        Files.createDirectories(out.getParent());
                        Files.copy(zis, out);
                    }
                }
                dir = tempDir;
                isZip = true;
            } else if (!Files.isDirectory(path)) {
                return VerificationResult.invalid(report, "path is not a directory or .aep file");
            }

            byte[] hashFile = readFile(dir, ai.aletheia.evidence.EvidencePackageServiceImpl.HASH_SHA256);
            byte[] canonicalFile = readFile(dir, ai.aletheia.evidence.EvidencePackageServiceImpl.CANONICAL_BIN);
            byte[] signatureFile = readFile(dir, ai.aletheia.evidence.EvidencePackageServiceImpl.SIGNATURE_SIG);
            byte[] timestampFile = readFile(dir, ai.aletheia.evidence.EvidencePackageServiceImpl.TIMESTAMP_TSR);
            byte[] publicKeyFile = readFile(dir, ai.aletheia.evidence.EvidencePackageServiceImpl.PUBLIC_KEY_PEM);

            if (hashFile == null || hashFile.length == 0) {
                return VerificationResult.invalid(report, "missing or empty hash.sha256");
            }
            if (canonicalFile == null) {
                return VerificationResult.invalid(report, "missing canonical.bin");
            }
            if (publicKeyFile == null || publicKeyFile.length == 0) {
                return VerificationResult.invalid(report, "missing or empty public_key.pem");
            }

            String hashHex = new String(hashFile, StandardCharsets.UTF_8).trim();
            if (hashHex.length() != 64 || !hashHex.matches("[0-9a-fA-F]+")) {
                return VerificationResult.invalid(report, "hash.sha256 must be 64 hex characters");
            }
            hashHex = hashHex.toLowerCase(Locale.ROOT);

            // (1) Hash check
            byte[] computedHash;
            try {
                computedHash = MessageDigest.getInstance("SHA-256").digest(canonicalFile);
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 not available", e);
            }
            String computedHex = HEX.formatHex(computedHash);
            if (!computedHex.equals(hashHex)) {
                report.add("hash: MISMATCH (computed " + computedHex + " != stored " + hashHex + ")");
                return VerificationResult.invalid(report, "hash mismatch");
            }
            report.add("hash: OK");

            // (2) Signature check
            PublicKey publicKey = loadPublicKeyFromPem(publicKeyFile);
            if (publicKey == null) {
                return VerificationResult.invalid(report, "failed to load public key from public_key.pem");
            }
            byte[] hashBytes = HEX.parseHex(hashHex);
            boolean sigValid = false;
            if (signatureFile != null && signatureFile.length > 0) {
                String sigBase64 = new String(signatureFile, StandardCharsets.UTF_8).trim().replaceAll("\\s+", "");
                if (!sigBase64.isEmpty()) {
                    try {
                        byte[] signatureBytes = Base64.getDecoder().decode(sigBase64);
                        sigValid = verifySignature(hashBytes, signatureBytes, publicKey);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            if (!sigValid) {
                report.add("signature: INVALID");
                return VerificationResult.invalid(report, "signature invalid");
            }
            report.add("signature: OK");

            // (3) TSA token
            if (timestampFile == null || timestampFile.length == 0) {
                report.add("timestamp: (none)");
            } else {
                String tsrBase64 = new String(timestampFile, StandardCharsets.UTF_8).trim().replaceAll("\\s+", "");
                if (tsrBase64.isEmpty()) {
                    report.add("timestamp: (empty)");
                } else {
                    try {
                        byte[] tsrBytes = Base64.getDecoder().decode(tsrBase64);
                        TimeStampToken token = new TimeStampToken(new CMSSignedData(tsrBytes));
                        String genTime = token.getTimeStampInfo().getGenTime() != null
                                ? token.getTimeStampInfo().getGenTime().toString()
                                : "(unknown)";
                        report.add("timestamp: " + genTime);

                        var certStore = token.getCertificates();
                        @SuppressWarnings("unchecked")
                        java.util.Collection<X509CertificateHolder> matches = certStore.getMatches(token.getSID());
                        if (matches != null && !matches.isEmpty()) {
                            X509CertificateHolder holder = matches.iterator().next();
                            X509Certificate cert = new JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(holder);
                            var signerVerifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(PROVIDER).build(cert);
                            if (!token.isSignatureValid(signerVerifier)) {
                                report.add("timestamp signature: INVALID");
                                return VerificationResult.invalid(report, "timestamp signature invalid");
                            }
                        }
                    } catch (Exception e) {
                        report.add("timestamp: INVALID (" + e.getMessage() + ")");
                        return VerificationResult.invalid(report, "timestamp invalid");
                    }
                }
            }

            // DP2.4: Optionally display claim and policy_version from metadata.json
            byte[] metadataFile = readFile(dir, ai.aletheia.evidence.EvidencePackageServiceImpl.METADATA_JSON);
            if (metadataFile != null && metadataFile.length > 0) {
                String metadataJson = new String(metadataFile, StandardCharsets.UTF_8);
                String claim = extractJsonString(metadataJson, "claim");
                String policyVer = extractJsonString(metadataJson, "policy_version");
                if (claim != null && !claim.isEmpty()) {
                    report.add("claim: " + (claim.length() > 80 ? claim.substring(0, 77) + "..." : claim));
                }
                if (policyVer != null && !policyVer.isEmpty()) {
                    report.add("policy_version: " + policyVer);
                }
            }

            return VerificationResult.valid(report);
        } catch (IOException e) {
            report.add("error: " + e.getMessage());
            return VerificationResult.invalid(report, "failed to read package: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                try {
                    deleteRecursively(tempDir);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /** Extract a string value for key from a minimal JSON object (no nested objects). */
    private static String extractJsonString(String json, String key) {
        if (json == null || key == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx = json.indexOf(':', idx);
        if (idx < 0) return null;
        idx = json.indexOf('"', idx + 1);
        if (idx < 0) return null;
        int start = idx + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == '"' || next == '\\') { sb.append(next); i++; continue; }
                if (next == 'n') { sb.append('\n'); i++; continue; }
                if (next == 'r') { sb.append('\r'); i++; continue; }
                if (next == 't') { sb.append('\t'); i++; continue; }
            }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
    }

    private static byte[] readFile(Path dir, String name) throws IOException {
        Path f = dir.resolve(name);
        if (!Files.isRegularFile(f)) return null;
        return Files.readAllBytes(f);
    }

    private static PublicKey loadPublicKeyFromPem(byte[] pemBytes) {
        try (Reader r = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(pemBytes), StandardCharsets.UTF_8);
             PEMParser parser = new PEMParser(r)) {
            Object obj = parser.readObject();
            if (obj instanceof org.bouncycastle.openssl.PEMKeyPair keyPair) {
                return new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter().setProvider(PROVIDER)
                        .getKeyPair(keyPair).getPublic();
            }
            if (obj instanceof java.security.cert.X509Certificate cert) {
                return cert.getPublicKey();
            }
            if (obj instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo spki) {
                return new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter().setProvider(PROVIDER).getPublicKey(spki);
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean verifySignature(byte[] hashBytes, byte[] signatureBytes, PublicKey publicKey) {
        if (hashBytes == null || hashBytes.length != SHA256_DIGEST_LENGTH || signatureBytes == null || signatureBytes.length == 0) {
            return false;
        }
        try {
            byte[] digestInfo = buildDigestInfo(hashBytes);
            Signature sig = Signature.getInstance("NONEwithRSA", PROVIDER);
            sig.initVerify(publicKey);
            sig.update(digestInfo);
            return sig.verify(signatureBytes);
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException("Verification failed", e);
        }
    }

    private static byte[] buildDigestInfo(byte[] hash) {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256);
        DigestInfo digestInfo = new DigestInfo(algId, hash);
        try {
            return digestInfo.getEncoded();
        } catch (IOException e) {
            throw new IllegalStateException("DigestInfo encoding failed", e);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                for (Path p : stream.toList()) {
                    deleteRecursively(p);
                }
            }
        }
        Files.delete(path);
    }
}
