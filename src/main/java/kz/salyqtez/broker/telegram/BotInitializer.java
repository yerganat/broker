package kz.salyqtez.broker.telegram;

import kz.salyqtez.broker.service.ExcelService;
import kz.salyqtez.broker.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer implements ApplicationListener<ApplicationReadyEvent> {
    @Value("${telegram.bot.name}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final ExcelService excelService;
    private final UserService userService;

    public BotInitializer(ExcelService excelService, UserService userService) {
        this.excelService = excelService;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new SalyqTezBot(botUsername, botToken, excelService, userService));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
