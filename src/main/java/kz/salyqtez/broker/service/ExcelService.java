package kz.salyqtez.broker.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import kz.salyqtez.broker.model.Excel;
import kz.salyqtez.broker.model.Exchange;
import kz.salyqtez.broker.repository.ExchangeRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
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

            if (start) {
                if (StringUtils.isBlank(currentRow.getCell(0).getStringCellValue())
                        || currentRow.getCell(0).getStringCellValue().contains("6")) {
                    break;
                }

                TicketDto ticket = new TicketDto();
                ticket.setTicker(currentRow.getCell(0).getStringCellValue());
                ticket.setType(currentRow.getCell(1).getStringCellValue());
                ticket.setPrice(currentRow.getCell(2).getNumericCellValue());
                ticket.setCount(Math.abs(currentRow.getCell(3).getNumericCellValue()));
                ticket.setTimestamp(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(currentRow.getCell(10).getStringCellValue())); //17.09.2019 11:48:09

                ticketList.add(ticket);
            }

            if (!start && currentRow.getCell(0).getStringCellValue().contains("Тиккер")) {
                start = true;
            }

        }

        workbook.close();


        return sortAndCalculate(ticketList);
    }

    private List<TicketDto> sortAndCalculate(List<TicketDto> ticketList) {
        ticketList.sort(Comparator.comparing(TicketDto::getTicker).thenComparing(TicketDto::getTimestamp));

//        String currentTicker = ""; TODO
        Double buyPrice = 0.0;

        for (TicketDto ticket : ticketList ) {

            if (ticket.getType().contains("Купля")) {
//                if (!currentTicker.equals(ticket.getTicker())) {
//                    currentTicker = ticket.getTicker();
//                }

                buyPrice = ticket.getPrice();
            }


            if (ticket.getType().contains("Продажа")) {
                if (ticket.getPrice() != null && ticket.getCount() != null
                        && ticket.getPrice() - buyPrice > 0.0) {
                    ticket.getCalc().setSumUsd(ticket.getCount() * (ticket.getPrice() - buyPrice));
                }

                if (ticket.getCalc().getSumUsd() != null) {
                    if (ticket.getTimestamp() != null) {
                        Date prevDate = getPrevDate(DateUtils.truncate(ticket.getTimestamp(), java.util.Calendar.DAY_OF_MONTH));
                        Exchange rate = rateRepository.findFirstByDate(prevDate);
                        if (rate != null) {
                            ticket.getCalc().setRate(rate.getRate());
                        }
                    }

                    if (ticket.getCalc().getRate() != null) {
                        ticket.getCalc().setSumKzt(ticket.getCalc().getSumUsd() * ticket.getCalc().getRate());
                        ticket.getCalc().setTax(ticket.getCalc().getSumKzt() / 10);
                    }
                }
            }
        }

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
            row.createCell(4).setCellValue(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(ticket.getTimestamp()));
            if (ticket.getCalc().getSumUsd() != null) {
                row.createCell(5).setCellValue(ticket.getCalc().getSumUsd());
            }
            if (ticket.getCalc().getRate() != null) {
                row.createCell(6).setCellValue(ticket.getCalc().getRate());
            }
            if (ticket.getCalc().getSumKzt() != null) {
                row.createCell(7).setCellValue(ticket.getCalc().getSumKzt());
            }
            if (ticket.getCalc().getTax() != null) {
                row.createCell(8).setCellValue(ticket.getCalc().getTax());
            }
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

    private Date getPrevDate(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
}
