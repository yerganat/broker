package kz.salyqtez.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.ApiContextInitializer;

@EnableJpaRepositories("kz.salyqtez.broker.repository")
@EntityScan("kz.salyqtez.broker.model")
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		ApiContextInitializer.init();
		SpringApplication.run(Application.class, args);
	}

}
