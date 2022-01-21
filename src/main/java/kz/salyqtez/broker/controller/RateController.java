package kz.salyqtez.broker.controller;

import kz.salyqtez.broker.exception.NotFoundException;
import kz.salyqtez.broker.model.Exchange;
import kz.salyqtez.broker.repository.ExchangeRepository;
import kz.salyqtez.broker.service.RateCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/api/rates")
public class RateController {

    private final ExchangeRepository rateRepository;

    public RateController(ExchangeRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    @GetMapping
    public Iterable findAll() {
        return rateRepository.findAll();
    }

    @GetMapping("/date/{rateDate}")
    public Exchange findByDate(@PathVariable @DateTimeFormat(pattern = "dd.MM.yyyy") Date rateDate) {
        return rateRepository.findFirstByDate(rateDate);
    }

    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException, ParseException {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        List<Exchange> exchangeList = new ArrayList<Exchange>();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();

            // skip header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            if(currentRow == null || currentRow.getCell(0) == null
                    || StringUtils.isBlank(currentRow.getCell(0).getStringCellValue())) {
                continue;
            }

            Exchange exchange = new Exchange();
            exchange.setDate(new SimpleDateFormat("dd.MM.yyyy").parse(currentRow.getCell(0).getStringCellValue()));
            exchange.setRate(currentRow.getCell(2).getNumericCellValue());

            exchangeList.add(exchange);
        }

        workbook.close();

        rateRepository.saveAll(exchangeList);

        response.sendRedirect("/rateShow");
    }

    @GetMapping("/{id}")
    public Exchange findOne(@PathVariable Long id) {
        return rateRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @GetMapping("/reload")
    public void reload(HttpServletResponse response) throws IOException {
        List<Exchange> rateList = rateRepository.findAll();

        RateCache.clear();
        for (Exchange rate:rateList) {
            RateCache.val.put(rate.getDate().getTime(), rate.getRate());
        }

        response.sendRedirect("/rateShow");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Exchange create(@RequestBody Exchange Exchange) {
        return rateRepository.save(Exchange);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        rateRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        rateRepository.deleteById(id);
    }
}
