package kz.salyqtez.broker.telegram;

import kz.salyqtez.broker.service.ExcelService;
import kz.salyqtez.broker.service.UserService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static kz.salyqtez.broker.Const.myBotUserId;
import static kz.salyqtez.broker.Const.spMailRu;

public class SalyqTezBot extends TelegramLongPollingBot {
    private String botUsername;

    private String botToken;

    private final ExcelService excelService;
    private final UserService userService;

    public SalyqTezBot(String botUsername, String botToken, ExcelService excelService, UserService userService) {
        super();
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.excelService = excelService;
        this.userService = userService;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        if (updates.get(0).getMessage() == null || updates.get(0).getMessage().getDocument() == null) {

//            InputStream videoIS = TicketDto.class.getClassLoader().getResourceAsStream("video.mp4");
//            SendVideo sendVideo = new SendVideo();
//            sendVideo.setChatId(updates.get(0).getMyChatMember().getChat().getId().toString());
//            sendVideo.setVideo(new InputFile(videoIS, "Инструкция.mp4"));
//            sendVideo.setCaption("Добро пожаловать!");


            try {
                SendMessage sdMessage = new SendMessage();
                if (updates.get(0).getMessage() == null) {
                    sdMessage.setChatId(updates.get(0).getMyChatMember().getChat().getId().toString());
                    userService.saveBlankUser(updates.get(0).getMyChatMember().getChat().getFirstName(), updates.get(0).getMyChatMember().getChat().getId(), null);

                    sdMessage.setText(excelService.getYoutubeLink());
                    execute(sdMessage);

                    sdMessage.setText("По вопросам обращайтесь на почту " + spMailRu);
                    execute(sdMessage);
                } else {
                    sdMessage.setChatId(updates.get(0).getMessage().getChatId().toString());
                    userService.saveBlankUser(updates.get(0).getMessage().getFrom().getFirstName(), updates.get(0).getMessage().getChatId(), updates.get(0).getMessage().getText());

                    if (updates.get(0).getMessage().getText().equals("pay")) {
                        InlineKeyboardButton payBtn = new InlineKeyboardButton();
                        payBtn.setText("Оплатить 555 тг");
                        payBtn.setUrl("https://paybox.money/kz_ru");
                        payBtn.setPay(true);

                        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                        List<InlineKeyboardButton> rowInline = new ArrayList<>();
                        rowInline.add(payBtn);
                        rowsInline.add(rowInline);
                        markupInline.setKeyboard(rowsInline);
                        sdMessage.setReplyMarkup(markupInline);
                    }

                    sdMessage.setText("По вопросам обращайтесь на почту " + spMailRu);
                    execute(sdMessage);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        if (updates.get(0).getMessage().getDocument() != null) {
            try {
                List<byte[]> excelContentList = new ArrayList<>();
                for (Update update : updates) {

                    if (!FilenameUtils.isExtension(update.getMessage().getDocument().getFileName(), "xlsx")) {
                        throw new RuntimeException("Файл должен быть в формате xlsx");
                    }

                    byte[] excelContent = downloadFromFileId(update.getMessage().getDocument().getFileId());
                    excelContentList.add(excelContent);

                    SendDocument sendToMe = new SendDocument();
                    sendToMe.setChatId(myBotUserId);
                    sendToMe.setDocument(new InputFile(new ByteArrayInputStream(excelContent), updates.get(0).getMessage().getDocument().getFileName()));
                    sendToMe.setCaption("UserId: " + updates.get(0).getMessage().getFrom().getId() + " FirstName: " + updates.get(0).getMessage().getFrom().getFirstName());
                    execute(sendToMe);
                }

                ExcelService.OutputDto outputDto = excelService.execute(updates.get(0).getMessage(), excelContentList);

                SendDocument sendDocumentRequest = new SendDocument();
                sendDocumentRequest.setChatId(updates.get(0).getMessage().getChatId().toString());
                sendDocumentRequest.setDocument(new InputFile(new ByteArrayInputStream(outputDto.bytes), "SALYQTEZ_" + (outputDto.isTamplate ? "шаблон.xlsx" : updates.get(0).getMessage().getDocument().getFileName())));
                sendDocumentRequest.setCaption("TAX");

                Message sendMessage = execute(sendDocumentRequest);
                excelService.updateSendFileId(sendMessage.getDocument().getFileId(), updates.get(0).getMessage().getFrom().getId(), updates.get(0).getMessage().getDate());
            } catch (Exception e) {
                try {
                    SendMessage messageIfError = new SendMessage();
                    messageIfError.setText("Ваш файл не соответствует формату Фридом финанс! Обратитесь в службу поддержки " + spMailRu);
                    messageIfError.setChatId(updates.get(0).getMessage().getChatId().toString());
                    execute(messageIfError);

                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));

                    SendDocument sendToMeError = new SendDocument();
                    sendToMeError.setChatId(myBotUserId);
                    sendToMeError.setDocument(new InputFile(new ByteArrayInputStream(sw.toString().getBytes()), "error.txt"));
                    sendToMeError.setCaption("Error: UserId: " + updates.get(0).getMessage().getFrom().getId() + " FirstName: " + updates.get(0).getMessage().getFrom().getFirstName());
                    execute(sendToMeError);

                    e.printStackTrace();
                } catch (TelegramApiException te) {
                    te.printStackTrace();
                }
            }

        }
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


        String command = update.getMessage().getText();

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

        if (command != null) {
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
