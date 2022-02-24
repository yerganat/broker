package kz.salyqtez.broker.controller;

import kz.salyqtez.broker.model.Setting;
import kz.salyqtez.broker.repository.SettingRepository;
import kz.salyqtez.broker.repository.UserRepository;
import kz.salyqtez.broker.telegram.BotSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static kz.salyqtez.broker.Const.youtubeLink;
import static kz.salyqtez.broker.Const.youtubeLinkName;

@Controller
public class LinkController {
    @Value("${spring.application.name}")
    String appName;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;

    public LinkController(SettingRepository settingRepository, UserRepository userRepository) {
        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/link")
    public String link(Model model) {
        Setting setting = settingRepository.findByName(youtubeLinkName);

        model.addAttribute("link", setting == null?youtubeLink:setting.getValue());

        return "link";
    }


    @PostMapping("/api/setting/link")
    @Transactional
    public void update(@RequestParam("link") String link, HttpServletResponse response) throws IOException, TelegramApiException {

        Setting setting = settingRepository.findByName(youtubeLinkName);
        if(setting == null) {
            setting = new Setting();
            setting.setName(youtubeLinkName);
            setting.setValue(link);
            settingRepository.save(setting);
        } else {
            settingRepository.updateSettings(link, youtubeLinkName);
        }



        BotSender botSender = new BotSender(botToken);


        SendMessage linkMessage = new SendMessage();
        linkMessage.setText(setting.getValue());

        List<String> botIds =  userRepository.findAllBotId();
        for (String botId: botIds) {
            linkMessage.setChatId(botId);
            botSender.execute(linkMessage);
        }

        response.sendRedirect("/link");
    }
}
