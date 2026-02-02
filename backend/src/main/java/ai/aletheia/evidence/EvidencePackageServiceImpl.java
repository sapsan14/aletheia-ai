package ai.aletheia.evidence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds Evidence Package files and ZIP per DP2.1.1 format.
 */
@Service
public class EvidencePackageServiceImpl implements EvidencePackageService {

    public static final String RESPONSE_TXT = "response.txt";
    public static final String CANONICAL_BIN = "canonical.bin";
    public static final String HASH_SHA256 = "hash.sha256";
    public static final String SIGNATURE_SIG = "signature.sig";
    public static final String TIMESTAMP_TSR = "timestamp.tsr";
    public static final String METADATA_JSON = "metadata.json";
    public static final String PUBLIC_KEY_PEM = "public_key.pem";

    private static final String[] ALL_FILES = {
            RESPONSE_TXT, CANONICAL_BIN, HASH_SHA256, SIGNATURE_SIG, TIMESTAMP_TSR, METADATA_JSON, PUBLIC_KEY_PEM
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, byte[]> buildPackage(
            String responseText,
            byte[] canonicalBytes,
            String hashHex,
            byte[] signatureBytes,
            byte[] tsaTokenBytes,
            String model,
            Instant createdAt,
            Long responseId,
            String publicKeyPem) {

        Map<String, byte[]> out = new LinkedHashMap<>();

        out.put(RESPONSE_TXT, (responseText != null ? responseText : "").getBytes(StandardCharsets.UTF_8));
        out.put(CANONICAL_BIN, canonicalBytes != null ? canonicalBytes : new byte[0]);
        out.put(HASH_SHA256, (hashHex != null ? hashHex : "").getBytes(StandardCharsets.UTF_8));
        out.put(SIGNATURE_SIG, signatureBytes != null ? Base64.getEncoder().encode(signatureBytes) : new byte[0]);
        out.put(TIMESTAMP_TSR, tsaTokenBytes != null ? Base64.getEncoder().encode(tsaTokenBytes) : new byte[0]);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("model", model != null ? model : "");
        metadata.put("created_at", createdAt != null ? createdAt.toString() : "");
        if (responseId != null) {
            metadata.put("response_id", responseId);
        }
        try {
            out.put(METADATA_JSON, objectMapper.writeValueAsBytes(metadata));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build metadata.json", e);
        }

        out.put(PUBLIC_KEY_PEM, (publicKeyPem != null ? publicKeyPem : "").getBytes(StandardCharsets.UTF_8));

        return out;
    }

    @Override
    public byte[] toZip(Map<String, byte[]> files) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String name : ALL_FILES) {
                byte[] content = files.get(name);
                if (content == null) {
                    content = new byte[0];
                }
                ZipEntry entry = new ZipEntry(name);
                entry.setSize(content.length);
                zos.putNextEntry(entry);
                zos.write(content);
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build Evidence Package ZIP", e);
        }
        return baos.toByteArray();
    }
}
