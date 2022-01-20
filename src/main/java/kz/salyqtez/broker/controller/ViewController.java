package kz.salyqtez.broker.controller;

import kz.salyqtez.broker.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @Value("${spring.application.name}")
    String appName;

    @Autowired
    private ExchangeRepository rateRepository;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        return "home";
    }

    @GetMapping("/rateUpload")
    public String rateUpload(Model model) {
        model.addAttribute("appName", "appName");
        return "rateUpload";
    }

    @GetMapping("/rateShow")
    public String rateShow(Model model) {
        model.addAttribute("rates", rateRepository.findAll());
        return "rateShow";
    }


    @GetMapping("/freedomExcel")
    public String freedomExcel(Model model) {
        model.addAttribute("appName", "appName");
        return "freedomExcel";
    }
}
