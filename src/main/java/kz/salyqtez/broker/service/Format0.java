package kz.salyqtez.broker.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Format0 {

    public static List<TicketDto> parse(Sheet sheet) throws ParseException {
        List<TicketDto> ticketList = new ArrayList<>();
        Iterator<Row> rows2 = sheet.iterator();
        rows2.next();
        while (rows2.hasNext()) {
            Row currentRow = rows2.next();

            if (currentRow.getCell(0) == null || StringUtils.isBlank(currentRow.getCell(0).getStringCellValue())) {
                continue;
            }

            TicketDto ticket = new TicketDto();
            ticket.setTicker(currentRow.getCell(0).getStringCellValue());
            ticket.setSellType(currentRow.getCell(1).getStringCellValue().contains("Продажа"));
            ticket.setPrice(currentRow.getCell(2).getNumericCellValue());
            ticket.setCount(Math.abs(currentRow.getCell(3).getNumericCellValue()));
            ticket.setTimestamp(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(currentRow.getCell(4).getStringCellValue())); //17.09.2019 11:48:09

            ticketList.add(ticket);
        }

        return ticketList;
    }
}
