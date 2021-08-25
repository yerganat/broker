package kz.salyqtez.broker.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

@Component
public class SalyqTezBot extends TelegramLongPollingBot {

    // Аннотация @Value позволяет задавать значение полю путем считывания из application.yaml
    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    public void onUpdateReceived(Update update) {

        System.out.println(update.getMessage().getText());
        System.out.println(update.getMessage().getFrom().getFirstName() );

        String command=update.getMessage().getText();

        SendMessage message = new SendMessage();

        message.setText("test");

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

        message.setChatId(update.getMessage().getChatId());


        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }
}
