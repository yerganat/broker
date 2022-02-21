package kz.salyqtez.broker.telegram;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class BotSender extends DefaultAbsSender {
    private String botToken;

    public BotSender(String botToken) {
        super(new DefaultBotOptions());
        this.botToken = botToken;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
