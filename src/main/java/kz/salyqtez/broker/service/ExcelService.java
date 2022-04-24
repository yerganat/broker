package kz.salyqtez.broker.service;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import kz.salyqtez.broker.model.Excel;
import kz.salyqtez.broker.model.Setting;
import kz.salyqtez.broker.repository.ExchangeRepository;
import kz.salyqtez.broker.repository.SettingRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
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
    private final PayboxService payboxService;


    public ExcelService(ExcelRepository excelRepository, ExchangeRepository rateRepository, SettingRepository settingRepository, PayboxService payboxService) {
        this.excelRepository = excelRepository;
        this.rateRepository = rateRepository;
        this.settingRepository = settingRepository;
        this.payboxService = payboxService;
    }


    private List<TicketDto> parse(Workbook workbook) throws IOException, NoSuchAlgorithmException, ParseException {

//        Workbook workbook = WorkbookFactory.create(inputStream);


        List<TicketDto> ticketList = new ArrayList<>();

        Integer tradeSheetIdx = null;

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().contains("Trades")) {
                tradeSheetIdx = i;
            }
            ;
        }

        if (tradeSheetIdx != null) {
            ticketList.addAll(FFormat2.parse(workbook.getSheetAt(tradeSheetIdx)));
        } else {
            ticketList.addAll(FFormat1.parse(workbook.getSheetAt(0)));
        }


        if (ticketList.size() == 0) {
            ticketList.addAll(Format0.parse(workbook.getSheetAt(0)));
        }

        workbook.close();


        return ticketList;
    }

    private List<TicketDto> sortAndCalculate(List<TicketDto> ticketList) {
        ticketList.sort(Comparator.comparing(TicketDto::getTicker).thenComparing(TicketDto::getTimestamp));

        for (TicketDto ticket : ticketList) {
//            if (ticket.isSell()) {
                if (ticket.getTimestamp() != null) {
//                    Exchange rate = rateRepository.findFirstByDate(prevDate);
//                    if (rate != null) {
//                        ticket.setRate(rate.getRate());
//                    }

                    Double rateVal = getRate(DateUtils.truncate(ticket.getTimestamp(), java.util.Calendar.DAY_OF_MONTH));
                    if (rateVal != null) {
                        ticket.setRate(rateVal);
                    }
                }
//            }
        }

        return ticketList;
    }

    private List<DivDto> sortAndCalculateDiv(List<DivDto> divList) {
        divList.sort(Comparator.comparing(DivDto::getDate));

        for (DivDto div : divList) {
            if (div.getType().trim().equals("Приход") && div.getDesc().contains("Div")
                    && div.getDate() != null) {
//                    Exchange rate = rateRepository.findFirstByDate(prevDate);
//                    if (rate != null) {
//                        ticket.setRate(rate.getRate());
//                    }

                Double rateVal = getRate(DateUtils.truncate(div.getDate(), java.util.Calendar.DAY_OF_MONTH));
                if (rateVal != null) {
                    div.setRate(rateVal);
                }
            }
        }

        return divList;
    }

    private Double getRate(Date ticketDate) {
        Date prevDate = ticketDate;
        for (int cnt = 0; cnt < 7; cnt++) {
            prevDate = getPrevDate(prevDate);
            Double rateVal = RateCache.val.get(prevDate.getTime());
            if (rateVal != null) {
                return rateVal;
            }
        }

        return null;
    }


    public OutputDto execute(MultipartFile file) throws IOException, NoSuchAlgorithmException, ParseException {
        List<TicketDto> ticketList = parse(new XSSFWorkbook(file.getInputStream()));
        List<DivDto> divList = new ArrayList<>(FFormatDiv1.parse(new XSSFWorkbook(file.getInputStream()).getSheetAt(0)));

        sortAndCalculate(ticketList);
        sortAndCalculateDiv(divList);

        return execute("system", null, null, file.getOriginalFilename(), null, file.getSize(), ticketList, excelChecksum(file.getInputStream()), divList);
    }

    public OutputDto execute(Message message, List<byte[]> excelContentList) throws IOException, NoSuchAlgorithmException, ParseException {
        List<TicketDto> ticketList = new ArrayList<>();
        List<DivDto> divList = new ArrayList<>();

        for (byte[] excelContent : excelContentList) {
            ticketList.addAll(parse(new XSSFWorkbook(new ByteArrayInputStream(excelContent))));
            divList.addAll(FFormatDiv1.parse(new XSSFWorkbook(new ByteArrayInputStream(excelContent)).getSheetAt(0)));
        }

        sortAndCalculate(ticketList);
        sortAndCalculateDiv(divList);

        payboxService.savePayment(message.getFrom().getId(), ticketList);
        return execute(message.getFrom().getFirstName(),
                message.getFrom().getId(),
                message.getDate(),
                message.getDocument().getFileName(),
                message.getDocument().getFileId(),
                (long) message.getDocument().getFileSize(),
                ticketList,
                excelChecksum(new ByteArrayInputStream(excelContentList.get(0))),
                divList);
    }

    private OutputDto execute(String user, Long userId, Integer timeNum, String fileName, String fileId, Long fileSize, List<TicketDto> ticketList, String fileHash, List<DivDto> divList) throws IOException, NoSuchAlgorithmException, ParseException {
        Excel excel = new Excel();
        excel.setUser(StringUtils.isNotBlank(user) ? user : userId.toString());
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();


        Workbook outWorkbook = new XSSFWorkbook();

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

        CellStyle backShortCS = outWorkbook.createCellStyle();
        backShortCS.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        backShortCS.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        backShortCS.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());

        CellStyle shortCS = outWorkbook.createCellStyle();
        shortCS.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        shortCS.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());

        CellStyle errorCS = outWorkbook.createCellStyle();
        errorCS.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        errorCS.setFillForegroundColor(IndexedColors.YELLOW.getIndex());


        int rowIdx = 1;
        Queue<ShortDto> shortQueue = new LinkedList<>();
        Queue<BuyDto> buyQueue = new LinkedList<>();
        for (TicketDto ticket : ticketList) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(ticket.getTicker());
            row.createCell(1).setCellValue(ticket.isSell() ? "Продажа" : "Покупка");
            row.createCell(2).setCellValue(ticket.getPrice());
            row.createCell(3).setCellValue(ticket.getCount());
            row.createCell(4).setCellValue(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(ticket.getTimestamp()));

            if (ticket.isBuy()) {
                Double buyCount = ticket.getCount();
                while (true) {
                    ShortDto shortDto = shortQueue.peek();
                    if (shortDto == null) {
                        break;
                    }

                    if (!shortDto.getTicker().equals(ticket.getTicker())) {
                        shortQueue.remove();
                        continue;
                    }

                    buyCount -= shortDto.getCount();
                    shortQueue.remove();

                    if(isPositive(sheet, shortDto.getRowIdx(),rowIdx)) {
                        Row shortRow = sheet.getRow(shortDto.getRowIdx() - 1);
                        String sumUsdFormula = "(C" + shortDto.getRowIdx() + "-" + "C" + rowIdx + ")*" + shortDto.getCount();
                        shortRow.createCell(5).setCellFormula(sumUsdFormula);
                        if (ticket.getRate() != null) {
                            row.createCell(6).setCellValue(ticket.getRate());
                            shortRow.createCell(7).setCellFormula("F" + shortDto.getRowIdx() + "*G" + rowIdx);
                            shortRow.createCell(8).setCellFormula("H" + shortDto.getRowIdx() + "/10");

                        }

                        styleRow(shortRow, shortCS);
                    }
                }


                if(buyCount>0) {
                    buyQueue.add(new BuyDto(ticket.getTicker(), rowIdx, buyCount));
                }

                if(buyCount < ticket.getCount()) {
                    Cell cell = row.createCell(9);
                    cell.setCellValue("Возврат долга по шорту!");

                    styleRow(row, backShortCS);
                }

            }

            if (ticket.isSell()) {
                Double sellCount = ticket.getCount();
                String sumUsdFormula = "";
                boolean isPositive = true;
                while (true) {
                    BuyDto buyDto = buyQueue.peek();
                    if (buyDto == null) {
                        break;
                    }

                    if (!buyDto.getTicker().equals(ticket.getTicker())) {
                        buyQueue.remove();
                        continue;
                    }

                    isPositive = isPositive(sheet, rowIdx, buyDto.getRowIdx());
                    if (buyDto.getCount() > sellCount) {
                        if (isPositive) {
                            sumUsdFormula += (StringUtils.isNotBlank(sumUsdFormula) ? " + " : "") + "(C" + rowIdx + "-" + "C" + buyDto.getRowIdx() + ")*" + sellCount;
                        }
                        buyDto.subCount(sellCount);
                        sellCount -= buyDto.getCount();
                        break;

                    } else {
                        if (isPositive) {
                            sumUsdFormula += (StringUtils.isNotBlank(sumUsdFormula) ? " + " : "") + "(C" + rowIdx + "-" + "C" + buyDto.getRowIdx() + ")*" + buyDto.getCount();
                        }
                        sellCount -= buyDto.getCount();
                        buyQueue.remove();

                        if (sellCount == 0.0) {
                            break;
                        }

                    }
                }

                if (sellCount.equals(ticket.getCount())) {
//                    sumUsdFormula = "C" + rowIdx + "*" + "D" + rowIdx;

                    Cell cell = row.createCell(9);
                    cell.setCellValue("Возможно игра в короткую(шорт)!");

                    styleRow(row, shortCS);

                    shortQueue.add(new ShortDto(ticket.getTicker(), rowIdx, ticket.getCount(), ticket.getPrice()));

                } else if (StringUtils.isNotBlank(sumUsdFormula)) {
                    row.createCell(5).setCellFormula(sumUsdFormula);
                    if (ticket.getRate() != null) {
                        row.createCell(6).setCellValue(ticket.getRate());
                        row.createCell(7).setCellFormula("F" + rowIdx + "*G" + rowIdx);
                        row.createCell(8).setCellFormula("H" + rowIdx + "/10");

                    }
                } else if (isPositive) {

                    Cell cell = row.createCell(9);
                    cell.setCellValue("Не хватает данных для расчета!");

                    styleRow(row, errorCS);
                }
            }
        }

        Row row = sheet.createRow(rowIdx);
        row.createCell(4).setCellValue("ИТОГО:");
        row.createCell(5).setCellFormula("SUM(F2:F"+rowIdx+")");
        row.createCell(7).setCellFormula("SUM(H2:H"+rowIdx+")");
        row.createCell(8).setCellFormula("SUM(I2:I"+rowIdx+")");

        addDividend(outWorkbook, divList);

        outWorkbook.write(out);

        return new OutputDto(out.toByteArray());
    }

    private void styleRow(Row row, CellStyle cellStyle) {
        row.setRowStyle(cellStyle);
        for(int i = 0; i<10; i++) {
            Cell cell  = row.getCell(i);
            if(cell != null) {
                cell.setCellStyle(cellStyle);
            }
        }
    }

    private void addDividend(Workbook outWorkbook, List<DivDto> divList) {
        String[] DIV_HEADERs = {"Дата", "Тип", "Приход", "Расход", "Примечание", "Курс", "KZT"};
        Sheet sheet = outWorkbook.createSheet("Dividend");

        for (int i = 0; i < 10; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, 5000);
        }

        sheet.setColumnWidth(4, 15000);


        // Header
        Row headerRow = sheet.createRow(0);

        for (int col = 0; col < DIV_HEADERs.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(DIV_HEADERs[col]);
        }

        int rowIdx = 1;
        for (DivDto div : divList) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(new SimpleDateFormat("dd.MM.yyyy").format(div.getDate()));
            row.createCell(1).setCellValue(div.getType());
            row.createCell(2).setCellValue(div.getIncome());
            row.createCell(3).setCellValue(div.getOutcome());
            row.createCell(4).setCellValue(div.getDesc());


            if (div.getRate() != null) {
                row.createCell(5).setCellValue(div.getRate());
                row.createCell(6).setCellFormula("F" + rowIdx + "*C" + rowIdx);
            }
        }

        Row row = sheet.createRow(rowIdx);
        row.createCell(5).setCellValue("ИТОГО:");
        row.createCell(6).setCellFormula("SUM(G2:G"+rowIdx+")");
    }


    private boolean isPositive(Sheet sheet, int row1Idx, int row2Idx) {
        CellReference c1Ref = new CellReference("C" + row1Idx);
        Cell c1Cell = sheet.getRow(c1Ref.getRow()).getCell(c1Ref.getCol());

        CellReference c2Ref = new CellReference("C" + row2Idx);
        Cell c2Cell = sheet.getRow(c2Ref.getRow()).getCell(c2Ref.getCol());

        return c1Cell.getNumericCellValue() - c2Cell.getNumericCellValue() > 0;
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
        Setting setting = settingRepository.findByName(youtubeLinkName);
        return setting == null ? youtubeLink : setting.getValue();
    }
}
