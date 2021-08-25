package kz.salyqtez.broker.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class SalyqTezBot extends TelegramLongPollingBot {

    // Аннотация @Value позволяет задавать значение полю путем считывания из application.yaml
    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public void onUpdatesReceived(List<Update> updates) {

    }

    @Override
    public String getBotUsername() {
        return "salyqtez_bot";
        //return botUsername;
    }

    @Override
    public String getBotToken() {
        return "1948042745:AAGOVe7PqBxb88MO1AOqngotrbOk0Xt3zWY";
        //return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update.getMessage().getText());
        System.out.println(update.getMessage().getFrom().getFirstName() );

        String command=update.getMessage().getText();

        SendMessage message = new SendMessage();

        message.setText("Hi!");

        if(command.equals("/myname")){
            System.out.println(update.getMessage().getFrom().getFirstName());
            message.setText(update.getMessage().getFrom().getFirstName());
        }

        if (command.equals("/mylastname")){
            System.out.println(update.getMessage().getFrom().getLastName());
            message.setText(update.getMessage().getFrom().getLastName());
        }

        if (command.equals("/myfullname")){
            System.out.println(update.getMessage().getFrom().getFirstName()+" "+update.getMessage().getFrom().getLastName());
            message.setText(update.getMessage().getFrom().getFirstName()+" "+update.getMessage().getFrom().getLastName());
        }

        message.setChatId(update.getMessage().getChatId().toString());


        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
