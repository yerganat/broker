package kz.salyqtez.broker.service;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import kz.salyqtez.broker.model.Excel;
import kz.salyqtez.broker.model.Exchange;
import kz.salyqtez.broker.model.Setting;
import kz.salyqtez.broker.repository.ExchangeRepository;
import kz.salyqtez.broker.repository.SettingRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kz.salyqtez.broker.repository.ExcelRepository;
import org.telegram.telegrambots.meta.api.objects.Message;

import static kz.salyqtez.broker.Const.youtubeLink;
import static kz.salyqtez.broker.Const.youtubeLinkName;


@Service
public class ExcelService {

    static String[] HEADERs = {"ТИКЕР", "ВИД", "ЦЕНА", "КОЛ-ВО", "ВРЕМЯ", "СУММА(USD)", "КУРС", "СУММА(KZT)", "НАЛОГ"};


    private final ExcelRepository excelRepository;
    private final ExchangeRepository rateRepository;
    private final SettingRepository settingRepository;


    public ExcelService(ExcelRepository excelRepository, ExchangeRepository rateRepository, SettingRepository settingRepository) {
        this.excelRepository = excelRepository;
        this.rateRepository = rateRepository;
        this.settingRepository = settingRepository;
    }


    private List<TicketDto> parse(Workbook workbook) throws IOException, NoSuchAlgorithmException, ParseException {

//        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        List<TicketDto> ticketList = new ArrayList<>();

        boolean start = false;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (currentRow.getCell(0) == null || !currentRow.getCell(0).getCellType().equals(CellType.STRING)) {
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

            if (!start
                    && currentRow.getCell(0).getCellType().equals(CellType.STRING)
                    && currentRow.getCell(1).getCellType().equals(CellType.STRING)
                    && currentRow.getCell(1).getStringCellValue().contains("Вид")
                    && (currentRow.getCell(0).getStringCellValue().contains("Тиккер")
                        || currentRow.getCell(0).getStringCellValue().contains("Тикер"))) {
                start = true;
            }

        }


        if (ticketList.size() == 0) {
            Iterator<Row> rows2 = sheet.iterator();
            rows2.next();
            while (rows2.hasNext()) {
                Row currentRow = rows2.next();

                if (currentRow.getCell(0) == null || StringUtils.isBlank(currentRow.getCell(0).getStringCellValue())) {
                    continue;
                }

                TicketDto ticket = new TicketDto();
                ticket.setTicker(currentRow.getCell(0).getStringCellValue());
                ticket.setType(currentRow.getCell(1).getStringCellValue());
                ticket.setPrice(currentRow.getCell(2).getNumericCellValue());
                ticket.setCount(Math.abs(currentRow.getCell(3).getNumericCellValue()));
                ticket.setTimestamp(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(currentRow.getCell(4).getStringCellValue())); //17.09.2019 11:48:09

                ticketList.add(ticket);
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
        List<TicketDto> ticketList = parse(new XSSFWorkbook(file.getInputStream()));

        return execute("system", null, null, file.getOriginalFilename(), null, file.getSize(), ticketList, excelChecksum(file.getInputStream()));
    }

    public OutputDto execute(Message message, List<byte[]> excelContentList) throws IOException, NoSuchAlgorithmException, ParseException {
        List<TicketDto> ticketList = new ArrayList<>();

        for (byte[] excelContent: excelContentList) {
            ticketList.addAll(parse(new XSSFWorkbook(new ByteArrayInputStream(excelContent))));
        }
        return execute(StringUtils.isBlank(message.getFrom().getFirstName())?message.getFrom().getFirstName():message.getFrom().getId().toString(),
                message.getFrom().getId(),
                message.getDate(),
                message.getDocument().getFileName(),
                message.getDocument().getFileId(),
                (long) message.getDocument().getFileSize(),
                ticketList,
                excelChecksum(new ByteArrayInputStream(excelContentList.get(0))));
    }

    public void saveBlankUser(Long userId, Integer date) {
        Excel excel = new Excel();
        excel.setUser(userId.toString());
        excel.setBotUserId(userId);
        excel.setBotActionTime(date);
        excel.setHash("");
        excel.setBytes(0);
        excelRepository.save(excel);
    }

    private OutputDto execute(String user, Long userId, Integer timeNum, String fileName, String fileId, Long fileSize, List<TicketDto> ticketList, String fileHash) throws IOException, NoSuchAlgorithmException, ParseException {
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
        sheet.setColumnWidth(9, 10000);


        // Header
        Row headerRow = sheet.createRow(0);

        for (int col = 0; col < HEADERs.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(HEADERs[col]);
        }

        int rowIdx = 1;

        String ticker = "";
        Integer startRowIdx = 0;
        Integer endRowIdx = 0;
        Double buyCount = 0.0;
        Map<Integer, Double> buyRowCountMap = new HashMap<>();
        for (TicketDto ticket : ticketList) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(ticket.getTicker());
            row.createCell(1).setCellValue(ticket.getType());
            row.createCell(2).setCellValue(ticket.getPrice());
            row.createCell(3).setCellValue(ticket.getCount());
            row.createCell(4).setCellValue(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(ticket.getTimestamp()));

            if (!ticker.equals(ticket.getTicker())) {
                ticker = ticket.getTicker();
                startRowIdx = rowIdx;
                endRowIdx = 0;
                buyRowCountMap = new HashMap<>();
            }

            if (ticket.getType().contains("Купля")) {
                buyRowCountMap.put(rowIdx, ticket.getCount());
                buyCount += ticket.getCount();
            }

            if (ticket.getType().contains("Продажа")) {
                if (endRowIdx == 0) {
                    endRowIdx = rowIdx;
                }


                Double sellCount = ticket.getCount();
                String sumUsdFormula = "";
                for (int buyRowIdx = startRowIdx; buyRowIdx < endRowIdx; buyRowIdx++) {
                    if (buyCount <= 0.0
                            || buyCount < sellCount
                            || buyCount == 0.0
                            || sellCount == 0.0) {
                        buyCount = 0.0;
                        break;
                    }

                    if (!buyRowCountMap.containsKey(buyRowIdx)) {
                        continue;
                    }

                    Double multpleCount = 0.0;
                    if (sellCount <= buyRowCountMap.get(buyRowIdx)) {
                        multpleCount = sellCount;
                        sellCount = 0.0;
                        buyRowCountMap.put(buyRowIdx, buyRowCountMap.get(buyRowIdx) - sellCount);
                    } else {
                        sellCount = sellCount - buyRowCountMap.get(buyRowIdx);
                        multpleCount = buyRowCountMap.get(buyRowIdx);
                        buyRowCountMap.remove(buyRowIdx);
                    }

                    buyCount -= multpleCount;

                    sumUsdFormula += (StringUtils.isNotBlank(sumUsdFormula)?" + ":"") +  "(C" + rowIdx + "-" + "C" + buyRowIdx + ")*" + multpleCount;


                }

                if (StringUtils.isNotBlank(sumUsdFormula)) {
                    row.createCell(5).setCellFormula(sumUsdFormula);
                    if (ticket.getRate() != null) {
                        row.createCell(6).setCellValue(ticket.getRate());
                        row.createCell(7).setCellFormula("F" + rowIdx + "*G" + rowIdx);
                        row.createCell(8).setCellFormula("H" + rowIdx + "/10");

                    }
                } else {
                    CellStyle errorCS = outWorkbook.createCellStyle();
                    errorCS.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    errorCS.setFillForegroundColor(IndexedColors.PINK.getIndex());

                    Cell cell = row.createCell(9);
                    cell.setCellStyle(errorCS);
                    cell.setCellValue("Нет хватает данных для расчета!");
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

    public String getYoutubeLink() {
        Setting setting= settingRepository.findByName(youtubeLinkName);
        return setting==null?youtubeLink:setting.getValue();
    }
}
