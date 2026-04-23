package com.arboviroses.conectaDengue.Utils.File;

import com.arboviroses.conectaDengue.Api.DTO.request.NotificationDataDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;

public class XlsxNotificationReader {

    /**
     * Mapeamento coluna XLSX -> setter no DTO.
     * Para adicionar coluna: incluir entrada aqui + campo correspondente em NotificationDataDTO.
     */
    private static final Set<String> DATE_COLUMNS = Set.of("DT_NOTIFIC", "DT_NASC");

    private static final Map<String, BiConsumer<NotificationDataDTO, String>> COLUMN_HANDLERS = new LinkedHashMap<>();

    static {
        COLUMN_HANDLERS.put("NU_NOTIFIC", (dto, v) -> dto.setNuNotific(parseLong(v)));
        COLUMN_HANDLERS.put("ID_AGRAVO",  NotificationDataDTO::setIdAgravo);
        COLUMN_HANDLERS.put("DT_NOTIFIC", NotificationDataDTO::setDtNotific);
        COLUMN_HANDLERS.put("DT_NASC",    NotificationDataDTO::setDtNasc);
        COLUMN_HANDLERS.put("CLASSI_FIN", NotificationDataDTO::setClassiFin);
        COLUMN_HANDLERS.put("CS_SEXO",    NotificationDataDTO::setCsSexo);
        COLUMN_HANDLERS.put("NM_BAIRRO",  NotificationDataDTO::setNmBairro);
        COLUMN_HANDLERS.put("ID_BAIRRO",  (dto, v) -> dto.setIdBairro(parseIntOrZero(v)));
        COLUMN_HANDLERS.put("EVOLUCAO",   NotificationDataDTO::setEvolucao);
        COLUMN_HANDLERS.put("NU_IDADE_N", (dto, v) -> dto.setIdade(parseIdadeSinan(v)));
    }

    public static List<NotificationDataDTO> read(InputStream inputStream) throws IOException {
        List<NotificationDataDTO> result = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Iterator<Row> rows = sheet.iterator();

            if (!rows.hasNext()) return result;

            Map<String, Integer> columnIndex = buildColumnIndex(rows.next());

            while (rows.hasNext()) {
                Row row = rows.next();
                NotificationDataDTO dto = new NotificationDataDTO();

                COLUMN_HANDLERS.forEach((colName, handler) -> {
                    Integer idx = columnIndex.get(colName);
                    if (idx == null) return;
                    String value = DATE_COLUMNS.contains(colName)
                            ? getDateCellValue(row.getCell(idx), evaluator)
                            : getCellValue(row.getCell(idx), evaluator);
                    if (!value.isBlank()) {
                        handler.accept(dto, value.trim());
                    }
                });

                result.add(dto);
            }
        }

        return result;
    }

    private static Map<String, Integer> buildColumnIndex(Row headerRow) {
        Map<String, Integer> index = new HashMap<>();
        for (Cell cell : headerRow) {
            String name = cell.getStringCellValue().trim();
            if (COLUMN_HANDLERS.containsKey(name)) {
                index.put(name, cell.getColumnIndex());
            }
        }
        return index;
    }

    private static String getDateCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> fmt.format(cell.getDateCellValue()); // funciona com ou sem formatação de data
                case STRING -> cell.getStringCellValue();            // já vem como string dd/MM/yyyy
                case FORMULA -> {
                    CellValue cv = evaluator.evaluate(cell);
                    yield cv.getCellType() == CellType.NUMERIC
                            ? fmt.format(DateUtil.getJavaDate(cv.getNumberValue()))
                            : cv.getStringValue();
                }
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private static String getCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue())
                    : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                CellValue cv = evaluator.evaluate(cell);
                yield switch (cv.getCellType()) {
                    case STRING -> cv.getStringValue();
                    case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                            ? new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue())
                            : String.valueOf(cv.getNumberValue());
                    case BOOLEAN -> String.valueOf(cv.getBooleanValue());
                    default -> "";
                };
            }
            default -> "";
        };
    }

    private static int parseIdadeSinan(String v) {
        try {
            int raw = Integer.parseInt(v.split("\\.")[0]);
            int unit = raw / 1000;  // 4=anos, 3=meses, 2=dias, 1=horas
            int value = raw % 1000;
            return unit == 4 ? value : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Long parseLong(String v) {
        try {
            return Long.parseLong(v.split("\\.")[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseIntOrZero(String v) {
        try {
            return Integer.parseInt(v.split("\\.")[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
