package nl.adg.rummikub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

// Beans are named by fully qualified class name: each spec generates into its own package, and two
// specs can produce the same simple name (GamesApiController in both gameroom and shared).
@SpringBootApplication(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class RummikubApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(RummikubApplication.class, args);
	}

	/** Entry point when the WAR is deployed into an external Tomcat instead of run with java -jar. */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(RummikubApplication.class);
	}

}
