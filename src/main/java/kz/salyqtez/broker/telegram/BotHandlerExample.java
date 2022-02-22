package kz.salyqtez.broker.telegram;

import java.io.*;

import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.vdurmont.emoji.EmojiParser; //https://www.webfx.com/tools/emoji-cheat-sheet/
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.*;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static java.lang.Math.toIntExact;

class BotHandlerExample extends TelegramLongPollingBot {
    private static final boolean TRACE_MODE = false;
    private final static int SIZE = 2000;
    private String smiley_emoji = EmojiParser.parseToUnicode(":smiley:");
    private String wink_emoji = EmojiParser.parseToUnicode(":wink:");
    private String share_number_emoji = EmojiParser.parseToUnicode(":phone: share your number");
    private String money_emoji = EmojiParser.parseToUnicode(":moneybag:");
    static String botName = "super";
    String response;

    public BotHandlerExample() {
    }


    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String textLine = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            if ((textLine == null) || (textLine.length() < 1))
                textLine = null;
            String request = textLine;
            ///////Just for Log memes :p Disabled by default
            if (false)
                System.out.println("test");
//                System.out.println("STATE=" + request + ":THAT=" + ((History) chatSession.thatHistory.get(0)).get(0) + ":TOPIC=" + chatSession.predicates.get("topic"));
            else if (request.equals("/start")) {
                response = " ";
                SendMessage message = new SendMessage(); // Create a message object object
                message.setChatId(String.valueOf(chat_id));
                message.setText("Hello!");
                // Create ReplyKeyboardMarkup object
                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                keyboardMarkup.setResizeKeyboard(true);
                // Create the keyboard (list of keyboard rows)
                List<KeyboardRow> keyboard = new ArrayList<>();
                // Create a keyboard row
                KeyboardRow row = new KeyboardRow();
                // Set each button, you can also use KeyboardButton objects if you need something else than text
                row.add("/Email");
                row.add("/Buy");
                // Add the first row to the keyboard
                keyboard.add(row);

                keyboardMarkup.setKeyboard(keyboard);
                // Add it to the message
                message.setReplyMarkup(keyboardMarkup);
                try {
                    execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (request.contains("/Email") || request.contains("/Buy")) {
                response = " ";

                if (update.getMessage().getText().equals("/Email")) {
                    SendMessage message = new SendMessage(); // Create a message object object
                    message.setChatId(String.valueOf(chat_id));
                    message.setText("Thank you so much for leaving feedback on me. It means a lot to my creators and to me.");
                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setUrl("https://mail.google.com");
                    inlineKeyboardButton.setCallbackData("Feed Back");
                    inlineKeyboardButton.setText("Email");
                    rowInline.add(inlineKeyboardButton);
                    // Set the keyboard to the markup
                    rowsInline.add(rowInline);
                    // Add it to the message
                    markupInline.setKeyboard(rowsInline);
                    message.setReplyMarkup(markupInline);
                    try {
                        execute(message); // Sending our message object to user
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                if (update.getMessage().getText().equals("/Buy")) {
                    SendMessage message = new SendMessage(); // Create a message object object
                    try {
                        execute(message); // Sending our message object to user
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

            } else if (update.hasCallbackQuery()) {
                // Set variables
                String call_data = update.getCallbackQuery().getData();
                long message_id = update.getCallbackQuery().getMessage().getMessageId();
                long chat_id1 = update.getCallbackQuery().getMessage().getChatId();
                response = " ";
                if (call_data.equals("Email")) {
                    String answer = "Updated message text";
                    EditMessageText new_message = new EditMessageText();
                    new_message.setChatId(String.valueOf(chat_id1));
                    new_message.setMessageId(toIntExact(message_id));
                    new_message.setText(answer);
                    try {
                        execute(new_message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

            } else if (request.equals("/help")) {
                response = "What's wrong man? ";
            } else {
                if (response.contains("<")) {
                    response = "Sorry, there was some error! " + wink_emoji;
                }

            }

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chat_id));
            message.setText(response);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    public String getBotUsername() {
        return "Talk_to_me_Bot";
    }

    @Override
    public String getBotToken() {
        return "170925sdzxfhcgvfgcdxy34567-sdvhgsdcwcw";
    }

    private static String getResourcesPath() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        System.out.println(path);
        String resourcesPath = path + File.separator + "src" + File.separator + "main" + File.separator + "resources";
        return resourcesPath;
    }
}
