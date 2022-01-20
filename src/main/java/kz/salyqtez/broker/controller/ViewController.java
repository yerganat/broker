package kz.salyqtez.broker.controller;

import kz.salyqtez.broker.model.Exchange;
import kz.salyqtez.broker.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ViewController {
    @Value("${spring.application.name}")
    String appName;

    private final ExchangeRepository rateRepository;

    public ViewController(ExchangeRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

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
    public String rateShow(@RequestParam(value = "year", required = false, defaultValue = "2022") String year,
                           @RequestParam(value = "dateStr", required = false) String dateStr,
                           Model model) throws ParseException {
        List<Exchange> rateList = new ArrayList<>();
        if(year != null) {
            rateList = rateRepository.findByDateBetween(
                    new SimpleDateFormat("yyyy-MM-dd").parse(year + "-01-01"),
                    new SimpleDateFormat("yyyy-MM-dd").parse(year + "-12-31")
            );
        }

        if(dateStr != null) {
            rateList = rateRepository.findByDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr));
        }

        model.addAttribute("rates", rateList);
        model.addAttribute("year", year);

        return "rateShow";
    }


    @GetMapping("/freedomExcel")
    public String freedomExcel(Model model) {
        model.addAttribute("appName", "appName");
        return "freedomExcel";
    }
}
