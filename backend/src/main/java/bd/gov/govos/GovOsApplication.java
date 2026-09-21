package bd.gov.govos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GovOsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GovOsApplication.class, args);
	}
}
