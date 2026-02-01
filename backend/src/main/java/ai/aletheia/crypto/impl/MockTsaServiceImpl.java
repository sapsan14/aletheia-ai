package ai.aletheia.crypto.impl;

import ai.aletheia.crypto.TimestampException;
import ai.aletheia.crypto.TimestampService;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * Deterministic MOCK_TSA implementation. No network — returns identical token for identical input.
 * <p>
 * Uses fixed time (2026-01-01 00:00:00 UTC) and mock key/cert from classpath. Same digest → same
 * serial → same signature → same token. Ideal for unit tests and CI.
 *
 * @see docs/en/MOCK_TSA.md
 */
@Service
@ConditionalOnProperty(name = "ai.aletheia.tsa.mode", havingValue = "mock", matchIfMissing = true)
public class MockTsaServiceImpl implements TimestampService {

    private static final Logger log = LoggerFactory.getLogger(MockTsaServiceImpl.class);
    private static final ASN1ObjectIdentifier POLICY_OID = new ASN1ObjectIdentifier("1.2.3.4.5.6");
    private static final Date FIXED_TIME = Date.from(java.time.Instant.parse("2026-01-01T00:00:00Z"));

    private TimeStampResponseGenerator responseGenerator;

    public MockTsaServiceImpl() {}

    @PostConstruct
    void init() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        try {
            var keyPair = loadKeyPair();
            var cert = loadCert();
            var signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(keyPair.getPrivate());
            DigestCalculatorProvider digestCalcProv = new JcaDigestCalculatorProviderBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build();
            DigestCalculator digestCalc = digestCalcProv.get(
                    new org.bouncycastle.asn1.x509.AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256));
            SignerInfoGenerator sigInfoGen = new JcaSignerInfoGeneratorBuilder(digestCalcProv)
                    .build(signer, cert);
            TimeStampTokenGenerator tokenGen = new TimeStampTokenGenerator(sigInfoGen, digestCalc, POLICY_OID);
            tokenGen.addCertificates(new JcaCertStore(List.of(cert)));
            this.responseGenerator = new TimeStampResponseGenerator(tokenGen, TSPAlgorithms.ALLOWED);
            log.info("MOCK_TSA initialized with fixed time {}", FIXED_TIME);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize MOCK_TSA", e);
        }
    }

    private java.security.KeyPair loadKeyPair() throws Exception {
        try (var reader = new InputStreamReader(
                new ClassPathResource("mock-tsa/mock-tsa.key").getInputStream(), StandardCharsets.UTF_8);
             var parser = new org.bouncycastle.openssl.PEMParser(reader)) {
            Object obj = parser.readObject();
            var converter = new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME);
            if (obj instanceof org.bouncycastle.openssl.PEMKeyPair keyPair) {
                return converter.getKeyPair(keyPair);
            }
            if (obj instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo) {
                java.security.PrivateKey priv = converter.getPrivateKey(privateKeyInfo);
                java.security.PublicKey pub = derivePublicFromPrivate(priv);
                return new java.security.KeyPair(pub, priv);
            }
            throw new IllegalStateException("Expected PEMKeyPair or PrivateKeyInfo in mock-tsa.key, got: "
                    + (obj == null ? "null" : obj.getClass().getName()));
        }
    }

    private static java.security.PublicKey derivePublicFromPrivate(java.security.PrivateKey privateKey) {
        if (privateKey instanceof java.security.interfaces.RSAPrivateCrtKey crt) {
            try {
                var spec = new java.security.spec.RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent());
                return java.security.KeyFactory.getInstance("RSA").generatePublic(spec);
            } catch (Exception e) {
                throw new IllegalStateException("Could not derive public key from RSA private key", e);
            }
        }
        throw new IllegalStateException("Only RSA private keys supported for mock TSA");
    }

    private X509Certificate loadCert() throws Exception {
        try (var is = new ClassPathResource("mock-tsa/mock-tsa.crt").getInputStream()) {
            var cf = java.security.cert.CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        }
    }

    @Override
    public byte[] timestamp(byte[] dataToTimestamp) {
        if (dataToTimestamp == null || dataToTimestamp.length == 0) {
            throw new IllegalArgumentException("Data to timestamp cannot be null or empty");
        }
        byte[] digest = sha256(dataToTimestamp);
        try {
            TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
            TimeStampRequest request = reqGen.generate(TSPAlgorithms.SHA256, digest);
            BigInteger serial = new BigInteger(1, digest);
            var response = responseGenerator.generateGrantedResponse(request, serial, FIXED_TIME);
            byte[] tokenBytes = response.getTimeStampToken().getEncoded();
            log.debug("MOCK_TSA returned token {} bytes", tokenBytes.length);
            return tokenBytes;
        } catch (Exception e) {
            throw new TimestampException("MOCK_TSA failed to generate token", e);
        }
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
