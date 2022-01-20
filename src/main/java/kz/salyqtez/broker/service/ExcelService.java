package kz.salyqtez.broker.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import kz.salyqtez.broker.model.Excel;
import kz.salyqtez.broker.model.Exchange;
import kz.salyqtez.broker.repository.ExchangeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import kz.salyqtez.broker.repository.ExcelRepository;

@Service
public class ExcelService {

    static String[] HEADERs = {"ТИКЕР", "ВИД", "ЦЕНА", "КОЛ-ВО", "ВРЕМЯ", "СУММА(USD)", "КУРС", "СУММА(KZT)", "НАЛОГ"};


    private final ExcelRepository excelRepository;

    private final ExchangeRepository rateRepository;

    public ExcelService(ExcelRepository excelRepository, ExchangeRepository rateRepository) {
        this.excelRepository = excelRepository;
        this.rateRepository = rateRepository;
    }

    private void save(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        Excel excel = new Excel();
        excel.setUser("system");
        excel.setName(file.getOriginalFilename());
        excel.setDescription(file.getName());
        excel.setProcessed(true);
        excel.setHash(excelChecksum(file.getInputStream()));
        excel.setBytes(file.getSize());
        excelRepository.save(excel);
    }

    private List<TicketDto> parse(MultipartFile file) throws IOException, NoSuchAlgorithmException, ParseException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        List<TicketDto> ticketList = new ArrayList<>();

        boolean start = false;
        while (rows.hasNext()) {
            Row currentRow = rows.next();

            if (!currentRow.getCell(0).getCellType().equals(CellType.STRING)) {
                continue;
            }

            if(start) {
                if (StringUtils.isEmpty(currentRow.getCell(0).getStringCellValue().trim())
                        || currentRow.getCell(0).getStringCellValue().contains("6")) {
                    break;
                }

                TicketDto ticket = new TicketDto();
                ticket.setTicker(currentRow.getCell(0).getStringCellValue());
                ticket.setType(currentRow.getCell(1).getStringCellValue());
                ticket.setPrice(currentRow.getCell(2).getNumericCellValue());
                ticket.setCount(currentRow.getCell(3).getNumericCellValue());
                ticket.setTimestamp(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(currentRow.getCell(10).getStringCellValue())); //17.09.2019 11:48:09
                ticketList.add(ticket);
            }

            if (!start && currentRow.getCell(0).getStringCellValue().contains("Тиккер")) {
                start = true;
            }

        }

        workbook.close();

        ticketList.sort(Comparator.comparing(TicketDto::getTicker));

        return ticketList;
    }


    public ByteArrayOutputStream execute(MultipartFile file) throws IOException, NoSuchAlgorithmException, ParseException {
        save(file);
        List<TicketDto> ticketList = parse(file);

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

        for (TicketDto ticket : ticketList) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(ticket.getTicker());
            row.createCell(1).setCellValue(ticket.getType());
            row.createCell(2).setCellValue(ticket.getPrice());
            row.createCell(3).setCellValue(ticket.getCount());
            row.createCell(4).setCellValue(ticket.getTimestamp());
        }

        workbook.write(out);

        return out;
    }

    private String excelChecksum(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        //Use SHA-1 algorithm
        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = inputStream.read(byteArray)) != -1) {
            shaDigest.update(byteArray, 0, bytesCount);
        }
        ;

        //close the stream; We don't need it now.
        inputStream.close();

        //Get the hash's bytes
        byte[] bytes = shaDigest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }
}
