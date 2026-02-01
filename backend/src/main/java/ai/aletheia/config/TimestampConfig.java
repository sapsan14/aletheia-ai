package ai.aletheia.config;

/**
 * TSA mode selection: mock (deterministic) or real (HTTP to external TSA).
 * <p>
 * Selection is done via {@link org.springframework.boot.autoconfigure.condition.ConditionalOnProperty}
 * on {@link ai.aletheia.crypto.impl.MockTsaServiceImpl} and {@link ai.aletheia.crypto.impl.RealTsaServiceImpl}.
 * <p>
 * Configure via:
 * <ul>
 *   <li>Environment: {@code AI_ALETHEIA_TSA_MODE}, {@code AI_ALETHEIA_TSA_URL}</li>
 *   <li>application.properties: {@code ai.aletheia.tsa.mode}, {@code ai.aletheia.tsa.url}</li>
 * </ul>
 * mode=mock → MockTsaServiceImpl (no network). mode=real → RealTsaServiceImpl (HTTP POST).
 *
 * @see docs/en/TIMESTAMPING.md
 */
public final class TimestampConfig {
    private TimestampConfig() {}
}
