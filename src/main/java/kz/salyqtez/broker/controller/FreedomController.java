package kz.salyqtez.broker.controller;

import kz.salyqtez.broker.helper.ExampleExcelHelper;
import kz.salyqtez.broker.service.ExcelService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

@RestController
@RequestMapping("/api/freedom")
public class FreedomController {

    private final ExcelService excelService;

    public FreedomController(ExcelService excelService) {
        this.excelService = excelService;
    }


    @PostMapping("/excel")
    public ResponseEntity<Resource> excel(@RequestParam("excel") MultipartFile file) throws IOException, NoSuchAlgorithmException, ParseException {

        if (!ExampleExcelHelper.hasExcelFormat(file)) {
            return ResponseEntity.badRequest().build();
        }

        ByteArrayOutputStream out = excelService.execute(file);

        HttpHeaders httpHeaders = new HttpHeaders();
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("SALYQ_"+file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();
        httpHeaders.setContentDisposition(contentDisposition);
        httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())),
                httpHeaders, HttpStatus.OK);
    }
}
