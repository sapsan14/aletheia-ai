package ai.aletheia.db.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA entity for the {@code ai_response} table.
 *
 * <p>Stores verifiable AI responses with cryptographic proof: prompt, response text,
 * SHA-256 hash of canonical response, optional RSA signature (Base64), optional RFC 3161
 * TSA token (Base64), and LLM metadata. Used for audit trail and verification.
 *
 * <p>Schema is defined in Flyway migration {@code V1__create_ai_response.sql}.
 * Signature and TSA token are stored as Base64 strings (VARCHAR) for portability.
 */
@Entity
@Table(name = "ai_response")
public class AiResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User prompt sent to the LLM. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    /** LLM response text (original, before canonicalization). */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    /** SHA-256 hash (64-char hex) of canonical response bytes. Immutable proof of content. */
    @Column(name = "response_hash", nullable = false, length = 64)
    private String responseHash;

    /** RSA signature (Base64) of the hash. Null when signing key not configured. */
    @Column(length = 2048)
    private String signature;

    /** PQC (ML-DSA) signature (Base64) of the same hash. Null when PQC disabled or key not configured. */
    @Column(name = "signature_pqc", length = 4096)
    private String signaturePqc;

    /** PQC public key PEM (stored when record saved with PQC). Used when building Evidence Package. */
    @Column(name = "pqc_public_key_pem", columnDefinition = "TEXT")
    private String pqcPublicKeyPem;

    /** RFC 3161 TSA token (Base64). Null when TSA not used or failed. */
    @Column(name = "tsa_token", length = 4096)
    private String tsaToken;

    /** LLM model identifier (e.g. "gpt-4", "gemini-pro"). */
    @Column(name = "llm_model", length = 255)
    private String llmModel;

    /** Record creation time (UTC). */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Optional request correlation ID for tracing. */
    @Column(name = "request_id", length = 64)
    private String requestId;

    /** LLM temperature parameter, if applicable. */
    private Double temperature;

    /** System prompt (if any) passed to the LLM. */
    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    /** Schema version for future migrations. */
    @Column(columnDefinition = "INTEGER DEFAULT 1")
    private Integer version = 1;

    /** DP2.4: Minimal AI Claim — claim text (e.g. first sentence of response). Null when not compliance. */
    @Column(columnDefinition = "TEXT")
    private String claim;

    /** DP2.4: Confidence in [0,1]. Null when not compliance. */
    @Column
    private Double confidence;

    /** DP2.4: Policy version (e.g. "gdpr-2024"). Null when not compliance. */
    @Column(name = "policy_version", length = 64)
    private String policyVersion;

    /** Phase 4: Policy coverage ratio in [0,1] for the demo policy. */
    @Column(name = "policy_coverage")
    private Double policyCoverage;

    /** Phase 4: JSON array of policy rule results (ruleId + status). */
    @Column(name = "policy_rules_evaluated", columnDefinition = "TEXT")
    private String policyRulesEvaluated;

    @PrePersist
    void onPersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (version == null) {
            version = 1;
        }
    }

    // --- Constructors ---

    /** Default constructor for JPA. */
    protected AiResponse() {}

    /** Minimal constructor for required fields. Use setters for optional fields. */
    public AiResponse(String prompt, String response, String responseHash) {
        this.prompt = prompt;
        this.response = response;
        this.responseHash = responseHash;
    }

    // --- Getters and setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getResponseHash() { return responseHash; }
    public void setResponseHash(String responseHash) { this.responseHash = responseHash; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getSignaturePqc() { return signaturePqc; }
    public void setSignaturePqc(String signaturePqc) { this.signaturePqc = signaturePqc; }

    public String getPqcPublicKeyPem() { return pqcPublicKeyPem; }
    public void setPqcPublicKeyPem(String pqcPublicKeyPem) { this.pqcPublicKeyPem = pqcPublicKeyPem; }

    public String getTsaToken() { return tsaToken; }
    public void setTsaToken(String tsaToken) { this.tsaToken = tsaToken; }

    public String getLlmModel() { return llmModel; }
    public void setLlmModel(String llmModel) { this.llmModel = llmModel; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getClaim() { return claim; }
    public void setClaim(String claim) { this.claim = claim; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }

    public Double getPolicyCoverage() { return policyCoverage; }
    public void setPolicyCoverage(Double policyCoverage) { this.policyCoverage = policyCoverage; }

    public String getPolicyRulesEvaluated() { return policyRulesEvaluated; }
    public void setPolicyRulesEvaluated(String policyRulesEvaluated) { this.policyRulesEvaluated = policyRulesEvaluated; }
}
