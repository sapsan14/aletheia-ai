package ai.aletheia;

import ai.aletheia.config.PqcSigningProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PqcSigningProperties.class)
public class AletheiaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AletheiaBackendApplication.class, args);
	}

}
