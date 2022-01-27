package kz.salyqtez.broker.service;

import java.io.*;
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
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kz.salyqtez.broker.repository.ExcelRepository;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class ExcelService {

    static String[] HEADERs = {"ТИКЕР", "ВИД", "КОЛ-ВО", "ЦЕНА",  "ВРЕМЯ", "СУММА(USD)", "КУРС", "СУММА(KZT)", "НАЛОГ"};


    private final ExcelRepository excelRepository;
    private final ExchangeRepository rateRepository;

    public ExcelService(ExcelRepository excelRepository, ExchangeRepository rateRepository) {
        this.excelRepository = excelRepository;
        this.rateRepository = rateRepository;
    }


    private List<TicketDto> parse(Workbook workbook) throws IOException, NoSuchAlgorithmException, ParseException {

//        Workbook workbook = WorkbookFactory.create(inputStream);

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

        for (TicketDto ticket : ticketList) {
            if (ticket.getType().contains("Продажа")) {
                if (ticket.getTimestamp() != null) {
                    Date prevDate = getPrevDate(DateUtils.truncate(ticket.getTimestamp(), java.util.Calendar.DAY_OF_MONTH));
                    Exchange rate = rateRepository.findFirstByDate(prevDate);
                    if (rate != null) {
                        ticket.setRate(rate.getRate());
                    }
                }
            }
        }

        return ticketList;
    }


    public OutputDto execute(MultipartFile file) throws IOException, NoSuchAlgorithmException, ParseException {
        return execute("system", null, null, file.getOriginalFilename(), null, file.getSize(), new XSSFWorkbook(file.getInputStream()), excelChecksum(file.getInputStream()));
    }

    public OutputDto execute(Message message, byte[] excelContent) throws IOException, NoSuchAlgorithmException, ParseException {
        return execute(message.getFrom().getFirstName(),
                message.getFrom().getId(),
                message.getDate(),
                message.getDocument().getFileName(),
                message.getDocument().getFileId(),
                (long) message.getDocument().getFileSize(),
                new XSSFWorkbook(new ByteArrayInputStream(excelContent)),
                excelChecksum(new ByteArrayInputStream(excelContent)));
    }

    private OutputDto execute(String user, Long userId, Integer timeNum, String fileName, String fileId, Long fileSize, Workbook workbook, String fileHash) throws IOException, NoSuchAlgorithmException, ParseException {
        Excel excel = new Excel();
        excel.setUser(user);
        excel.setName(fileName);
        excel.setDescription(fileName);
        excel.setProcessed(true);
        excel.setHash(fileHash);
        excel.setBytes(fileSize);
        excel.setBotUserId(userId);
        excel.setBotActionTime(timeNum);
        excel.setBotFileId(fileId);
        excelRepository.save(excel);

        List<TicketDto> ticketList = parse(workbook);

        if (ticketList.size() == 0) {
            InputStream templateIS = TicketDto.class.getClassLoader().getResourceAsStream("шаблон.xlsx");

            ByteArrayOutputStream templateOS = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytes = 0;
            while ((bytes = templateIS.read(buffer, 0, buffer.length)) > 0) {
                templateOS.write(buffer, 0, bytes);
            }
            return new OutputDto(templateOS.toByteArray(), true);
        }

        Workbook outWorkbook = new XSSFWorkbook();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Sheet sheet = outWorkbook.createSheet("Calculated Taxes");

        for (int i = 0; i < 10; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, 5000);
        }


        // Header
        Row headerRow = sheet.createRow(0);

        for (int col = 0; col < HEADERs.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(HEADERs[col]);
        }

        int rowIdx = 1;

        String ticker = "";
        Double buyCount = 0.0;
        Integer startRowIdx = 0;
        Integer endRowIdx = 0;
        for (TicketDto ticket : ticketList) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(ticket.getTicker());
            row.createCell(1).setCellValue(ticket.getType());
            row.createCell(2).setCellValue(ticket.getPrice());
            row.createCell(3).setCellValue(ticket.getCount());
            row.createCell(4).setCellValue(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(ticket.getTimestamp()));

            if (!ticker.equals(ticket.getTicker())) {
                ticker = ticket.getTicker();
                buyCount = 0.0;
                startRowIdx = rowIdx;
                endRowIdx = 0;
            }

            if (ticket.getType().contains("Купля")) {
                buyCount += ticket.getCount();
            }

            if (ticket.getType().contains("Продажа")) {
                if (endRowIdx == 0) {
                    endRowIdx = rowIdx;
                }


                String sumUsdFormula = "";
                for (int buyRowIdx = startRowIdx; buyRowIdx < endRowIdx; buyRowIdx++) {
                    sumUsdFormula = "C" + buyRowIdx;
                }

                row.createCell(6).setCellValue(ticket.getRate());

                if (StringUtils.isNotBlank(sumUsdFormula)) {
                    buyCount -= ticket.getCount();
                    if(buyCount >= 0) {
                        row.createCell(5).setCellFormula("(C" + rowIdx + "-" + sumUsdFormula + ")*D" + rowIdx);
                        if (ticket.getRate() != null) {
                            row.createCell(7).setCellFormula("F" + rowIdx + "*G" + rowIdx);
                            row.createCell(8).setCellFormula("H" + rowIdx + "/10");

                        }
                    } else {
                        CellStyle errorCS = outWorkbook.createCellStyle();
                        errorCS.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        errorCS.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());

                        Cell cell = row.createCell(9);
                        cell.setCellStyle(errorCS);
                        cell.setCellValue("Нет хватает данных для расчета!");
                    }
                }
            }
        }

        outWorkbook.write(out);

        return new OutputDto(out.toByteArray());
    }


    @Transactional
    public void updateSendFileId(String botSendFileId, Long userId, Integer actionTime) {
        excelRepository.updateSendFileId(botSendFileId, userId, actionTime);
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

    public static class OutputDto {
        public byte[] bytes;
        public boolean isTamplate = false;

        public OutputDto(byte[] bytes) {
            this.bytes = bytes;
        }

        public OutputDto(byte[] bytes, boolean isTamplate) {
            this.bytes = bytes;
            this.isTamplate = isTamplate;
        }
    }

}
