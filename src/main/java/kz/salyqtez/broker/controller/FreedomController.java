package kz.salyqtez.broker.controller;

import kz.salyqtez.broker.exception.NotFoundException;
import kz.salyqtez.broker.helper.ExampleExcelHelper;
import kz.salyqtez.broker.model.Exchange;
import kz.salyqtez.broker.repository.ExchangeRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/api/freedom")
public class FreedomController {

    static String[] HEADERs = { "ТИККЕР",	"ВИД",	"ЦЕНА",	"КОЛ-ВО",	"ВРЕМЯ", "СУММА(USD)", "КУРС", "СУММА(KZT)", "НАЛОГ" };

    @Autowired
    private ExchangeRepository rateRepository;

    @GetMapping
    public Iterable findAll() {
        return rateRepository.findAll();
    }

    @PostMapping("/excel")
    public ResponseEntity<Resource> excel(@RequestParam("excel") MultipartFile excel, HttpServletResponse response) throws IOException, ParseException {

        if (!ExampleExcelHelper.hasExcelFormat(excel)) {
            return ResponseEntity.badRequest().build();
        }

        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Sheet sheet = workbook.createSheet("Calculated");

        // Header
        Row headerRow = sheet.createRow(0);

        for (int col = 0; col < HEADERs.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(HEADERs[col]);
        }

        int rowIdx = 1;

        List<Exchange> rates = rateRepository.findAll();
        for (Exchange rate : rates) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(rate.getDate());
            row.createCell(1).setCellValue(rate.getRate());
        }

        workbook.write(out);


        InputStreamResource file = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "Calculated.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
}
