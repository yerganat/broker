package kz.salyqtez.broker.controller;

import kz.salyqtez.broker.helper.ExampleExcelHelper;
import kz.salyqtez.broker.repository.UserRepository;
import kz.salyqtez.broker.service.ExcelService;
import kz.salyqtez.broker.service.UserService;
import kz.salyqtez.broker.telegram.BotSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SendController {
    @Value("${spring.application.name}")
    String appName;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final UserRepository userRepository;
    private final UserService userService;

    public SendController(UserRepository userRepository,
                          UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }


    @GetMapping("/send")
    public String send(Model model) {

        model.addAttribute("send", "hello");

        return "send";
    }


    @PostMapping("/api/setting/send")
    @Transactional
    public void send(@RequestParam("excel") MultipartFile file, @RequestParam("txt") String txt, @RequestParam("botId") String botId, HttpServletResponse response) throws IOException, ParseException, NoSuchAlgorithmException, TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setText(txt);

        List<String> botIds = new ArrayList<>();
        if(StringUtils.isBlank(botId)) {
            botIds = userRepository.findAllBotId();
        } else {
            botIds = List.of(botId);
        }

        for (String bID: botIds) {
            msg.setChatId(bID);
            new BotSender(botToken).execute(msg);
        }

        String fileName = "нет";
        if (ExampleExcelHelper.hasExcelFormat(file) && botId != null) {
            fileName = "SALYQTEZ_"+file.getOriginalFilename();

//            ExcelService.OutputDto outputDto = excelService.execute(file);
            SendDocument sendDocumentRequest = new SendDocument();
            sendDocumentRequest.setChatId(botId);
            sendDocumentRequest.setDocument(new InputFile(file.getInputStream(), fileName));
            sendDocumentRequest.setCaption("TAX");


            new BotSender(botToken).execute(sendDocumentRequest);
        }


        if(botId != null && !botId.isEmpty()) {
            userService.saveBlankUser("system", Long.valueOf(botId), msg + "; файл: " + fileName);
        }

        response.sendRedirect("/");
    }
}
