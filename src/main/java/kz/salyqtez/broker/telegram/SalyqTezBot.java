package kz.salyqtez.broker.telegram;

import kz.salyqtez.broker.service.ExcelService;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;

public class SalyqTezBot extends TelegramLongPollingBot {
    private String botUsername;

    private String botToken;

    private final ExcelService excelService;

    public SalyqTezBot(String botUsername, String botToken, ExcelService excelService) {
        super();
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.excelService = excelService;
    }
    @Override
    public void onUpdatesReceived(List<Update> updates) {
        this.onUpdateReceived(updates.get(0));
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.getMessage().getDocument() != null) {
            try {
                byte[] excelContent = downloadFromFileId(update.getMessage().getDocument().getFileId());

                ExcelService.OutputDto outputDto = excelService.execute(update.getMessage(), excelContent);

                SendDocument sendDocumentRequest = new SendDocument();
                sendDocumentRequest.setChatId(update.getMessage().getChatId().toString());
                sendDocumentRequest.setDocument(new InputFile(new ByteArrayInputStream(outputDto.bytes), "SALYQTEZ_" + (outputDto.isTamplate?"шаблон.xlsx" :update.getMessage().getDocument().getFileName())));
                sendDocumentRequest.setCaption("TAX");

                Message sendMessage = execute(sendDocumentRequest);
                excelService.updateSendFileId(sendMessage.getDocument().getFileId(), update.getMessage().getFrom().getId(), update.getMessage().getDate());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

//            GetFile request = new GetFile(update.getMessage().getDocument().getFileId());
        }


        String command=update.getMessage().getText();



        SendMessage message = new SendMessage();

        message.setText("Coming soon, " + update.getMessage().getFrom().getFirstName() + "!");

//        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//        List<InlineKeyboardButton> rowInline = new ArrayList<>();
//
//        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
//        inlineKeyboardButton.setText("Update message text");
//        inlineKeyboardButton.setCallbackData("update_msg_text");
//        rowInline.add(inlineKeyboardButton);
//        // Set the keyboard to the markup
//        rowsInline.add(rowInline);
//        // Add it to the message
//        markupInline.setKeyboard(rowsInline);
//        message.setReplyMarkup(markupInline);

        if(command != null) {
            if (command.equals("/myname")) {
                message.setText(update.getMessage().getFrom().getFirstName());
            }

            if (command.equals("/mylastname")) {
                message.setText(update.getMessage().getFrom().getLastName());
            }

            if (command.equals("/myfullname")) {
                message.setText(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName());
            }
        }

        message.setChatId(update.getMessage().getChatId().toString());


//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }

    }

    private byte[] downloadFromFileId(String fileId) throws TelegramApiException, IOException {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        File file = execute(getFile);
        URL fileUrl = new URL(file.getFileUrl(botToken));
        HttpURLConnection httpConn = (HttpURLConnection) fileUrl.openConnection();
        InputStream inputStream = httpConn.getInputStream();
        byte[] output = IOUtils.toByteArray(inputStream);

        String fileName = file.getFilePath();
        String[] fileNameSplitted = fileName.split("\\.");
        String extension = fileNameSplitted[fileNameSplitted.length - 1];
        String filenameWithoutExtension = fileName.substring(0, fileName.length() - extension.length() - 1);

        inputStream.close();
        httpConn.disconnect();

        return output;
    }

}
