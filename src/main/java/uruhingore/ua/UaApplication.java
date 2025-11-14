package uruhingore.ua;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UaApplication {

	public static void main(String[] args) {
		// Load .env file before Spring Boot starts
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();
			
			// Set system properties from .env file
			dotenv.entries().forEach(entry -> {
				String key = entry.getKey();
				String value = entry.getValue();
				// Only set if not already set as system property or environment variable
				if (System.getProperty(key) == null && System.getenv(key) == null) {
					System.setProperty(key, value);
				}
			});
		} catch (Exception e) {
			// If .env file doesn't exist, that's okay - use environment variables instead
			System.err.println("Warning: Could not load .env file: " + e.getMessage());
		}
		
		SpringApplication.run(UaApplication.class, args);
	}

}
