package ai.aletheia.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies PQC.1: PqcSigningProperties is loaded and defaults are correct when PQC is not configured.
 */
@SpringBootTest
@ActiveProfiles("default")
class PqcSigningPropertiesTest {

	@Autowired
	private PqcSigningProperties pqcSigningProperties;

	@Test
	void contextLoadsAndPqcPropertiesHaveDefaults() {
		assertThat(pqcSigningProperties).isNotNull();
		assertThat(pqcSigningProperties.isPqcEnabled()).isFalse();
		assertThat(pqcSigningProperties.getPqcKeyPath()).isEmpty();
	}
}
