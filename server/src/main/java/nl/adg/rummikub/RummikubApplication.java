package nl.adg.rummikub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
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
