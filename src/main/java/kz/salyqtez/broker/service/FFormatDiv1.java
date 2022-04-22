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

public class FFormatDiv1 {

    public static List<DivDto> parse(Sheet sheet) throws ParseException {
        List<DivDto> dtoList = new ArrayList<>();

        Iterator<Row> rows = sheet.iterator();
        boolean start = false;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (currentRow.getCell(0) == null || !currentRow.getCell(0).getCellType().equals(CellType.STRING)) {
                continue;
            }
            if (start) {
                if (StringUtils.isBlank(currentRow.getCell(0).getStringCellValue())
                        || currentRow.getCell(0).getStringCellValue().trim().startsWith("3.")) {
                    break;
                }

                DivDto div = new DivDto();
                div.setDate(new SimpleDateFormat("dd.MM.yyyy").parse(currentRow.getCell(0).getStringCellValue()));
                div.setType(currentRow.getCell(1).getStringCellValue());
                div.setIncome(currentRow.getCell(2).getNumericCellValue());
                div.setOutcome(currentRow.getCell(3).getNumericCellValue());
                div.setDesc(currentRow.getCell(4).getStringCellValue());


                dtoList.add(div);
            }

            if (!start
                    && currentRow.getCell(0) != null
                    && currentRow.getCell(1) != null
                    && currentRow.getCell(0).getCellType().equals(CellType.STRING)
                    && currentRow.getCell(1).getCellType().equals(CellType.STRING)
                    && currentRow.getCell(1).getStringCellValue().contains("Тип")
                    && currentRow.getCell(0).getStringCellValue().contains("Дата")) {
                start = true;
            }

        }

        return dtoList;
    }
}
