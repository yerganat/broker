package kz.salyqtez.broker.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FFormat2 {

    public static List<TicketDto> parse(Sheet sheet) throws ParseException {
        List<TicketDto> ticketList = new ArrayList<>();

        Iterator<Row> rows = sheet.iterator();
        boolean firstRowSkiped = false;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (!firstRowSkiped) {
                firstRowSkiped = true;
                continue;
            }
            TicketDto ticket = new TicketDto();
            ticket.setTicker(currentRow.getCell(0).getStringCellValue());
            ticket.setSellType(currentRow.getCell(3).getStringCellValue().contains("Продажа"));
            ticket.setPrice(currentRow.getCell(5).getNumericCellValue());
            ticket.setCount(Math.abs(currentRow.getCell(4).getNumericCellValue()));
            ticket.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentRow.getCell(11).getStringCellValue())); //17.09.2019 11:48:09

            ticketList.add(ticket);
        }


        return ticketList;
    }
}
