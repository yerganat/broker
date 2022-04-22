package kz.salyqtez.broker.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FFormat1 {

    public static List<TicketDto> parse(Sheet sheet) throws ParseException {
        List<TicketDto> ticketList = new ArrayList<>();

        Iterator<Row> rows = sheet.iterator();
        boolean start = false;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (currentRow.getCell(0) == null || !currentRow.getCell(0).getCellType().equals(CellType.STRING)) {
                continue;
            }
            if (start) {
                if (StringUtils.isBlank(currentRow.getCell(0).getStringCellValue())
                        || currentRow.getCell(0).getStringCellValue().trim().startsWith("6.")) {
                    break;
                }

                TicketDto ticket = new TicketDto();
                ticket.setTicker(currentRow.getCell(0).getStringCellValue());
                ticket.setSell(currentRow.getCell(1).getStringCellValue().contains("Продажа"));
                ticket.setPrice(currentRow.getCell(2).getNumericCellValue());
                ticket.setCount(Math.abs(currentRow.getCell(3).getNumericCellValue()));

                if(currentRow.getCell(10).getCellType().equals(CellType.STRING)) {
                    ticket.setTimestamp(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(currentRow.getCell(10).getStringCellValue())); //17.09.2019 11:48:09
                } else {
                    ticket.setTimestamp(currentRow.getCell(10).getDateCellValue());
                }

                ticketList.add(ticket);
            }

            if (!start
                    && currentRow.getCell(0) != null
                    && currentRow.getCell(1) != null
                    && currentRow.getCell(0).getCellType().equals(CellType.STRING)
                    && currentRow.getCell(1).getCellType().equals(CellType.STRING)
                    && currentRow.getCell(1).getStringCellValue().contains("Вид")
                    && (currentRow.getCell(0).getStringCellValue().contains("Тиккер")
                    || currentRow.getCell(0).getStringCellValue().contains("Тикер"))) {
                start = true;
            }

        }

        return ticketList;
    }
}
