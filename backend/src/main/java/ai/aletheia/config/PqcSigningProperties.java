package ai.aletheia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for optional PQC (post-quantum) signing.
 * When {@link #isPqcEnabled()} is true and {@link #getPqcKeyPath()} is set,
 * the backend can produce a second ML-DSA (Dilithium) signature over the same hash.
 *
 * @see docs/en/PLAN_PQC.md
 */
@ConfigurationProperties(prefix = "ai.aletheia.signing")
public class PqcSigningProperties {

    /** If true and pqc-key-path is set, PQC signing is attempted. Default: false. */
    private boolean pqcEnabled = false;

    /** Path to ML-DSA private key (PEM or key file). Empty = no PQC. */
    private String pqcKeyPath = "";

    public boolean isPqcEnabled() {
        return pqcEnabled;
    }

    public void setPqcEnabled(boolean pqcEnabled) {
        this.pqcEnabled = pqcEnabled;
    }

    public String getPqcKeyPath() {
        return pqcKeyPath;
    }

    public void setPqcKeyPath(String pqcKeyPath) {
        this.pqcKeyPath = pqcKeyPath != null ? pqcKeyPath : "";
    }
}
