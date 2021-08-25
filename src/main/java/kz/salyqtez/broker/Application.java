package kz.salyqtez.broker;

import kz.salyqtez.broker.telegram.SalyqTezBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@EnableJpaRepositories("kz.salyqtez.broker.repository")
@EntityScan("kz.salyqtez.broker.model")
@SpringBootApplication
public class Application {

	public static void main(String[] args) {

		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new SalyqTezBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		SpringApplication.run(Application.class, args);
	}

}
