package com.arboviroses.conectaDengue.Utils.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import com.arboviroses.conectaDengue.Api.DTO.response.SaveCsvResponseDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.NotificationWithError;
import com.arboviroses.conectaDengue.Utils.MossoroData.NeighborhoodsMossoro;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import static com.arboviroses.conectaDengue.Utils.ConvertCSVLineToNotifications.convertCsvLineToNotificationObject;

/**
 * Classe mantida para compatibilidade com arquivos CSV antigos.
 * O novo endpoint recebe somente texto
 */
@Deprecated
public class DataToNotificationObject {
    
    /**
     * Método original mantido para compatibilidade
     */
    public static List<Notification> processCSVFile(MultipartFile file) throws IOException, CsvException {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            NeighborhoodsMossoro neighborhoods = new NeighborhoodsMossoro();
            List<String[]> csvLines = csvReader.readAll();
            List<Notification> notifications = new ArrayList<>();
            List<String> header = Arrays.asList(csvLines.get(0));
            csvLines.remove(0);

            for (String[] csvLine : csvLines) {
                try {
                    String neighborhoodFromNotification = csvLine[header.indexOf("NM_BAIRRO")];

                    if ((neighborhoodFromNotification = neighborhoods.search(neighborhoodFromNotification)) != null) {
                        csvLine[header.indexOf("NM_BAIRRO")] = neighborhoodFromNotification;
                        Notification notification = convertCsvLineToNotificationObject(csvLine, header);
                        notifications.add(notification);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return notifications;
        }
    }

    /**
     * Novo método para processar XLSX com streaming e economia de memória
     */
    @Deprecated
    public static SaveCsvResponseDTO processXLSXFileStreaming(
            InputStream inputStream,
            List<Notification> notificationBatch,
            List<NotificationWithError> errorBatch,
            Long currentIteration,
            int batchSize,
            BiConsumer<List<Notification>, List<NotificationWithError>> saveBatches,
            BiConsumer<List<Notification>, List<NotificationWithError>> clearBatches) throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            NeighborhoodsMossoro neighborhoods = new NeighborhoodsMossoro();

            if (!rowIterator.hasNext()) {
                throw new IOException("O arquivo está vazio.");
            }

            Row headerRow = rowIterator.next();
            List<String> header = new ArrayList<>();
            for (Cell cell : headerRow) {
                header.add(cell.getStringCellValue());
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    processXLSXRow(row, header, evaluator, neighborhoods, 
                                 notificationBatch, errorBatch, currentIteration);

                    if (notificationBatch.size() >= batchSize) {
                        saveBatches.accept(notificationBatch, errorBatch);
                        clearBatches.accept(notificationBatch, errorBatch);
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar linha do XLSX: " + e.getMessage());
                }
            }

            if (!notificationBatch.isEmpty() || !errorBatch.isEmpty()) {
                saveBatches.accept(notificationBatch, errorBatch);
            }

            return new SaveCsvResponseDTO(true);
        }
    }

    private static void processXLSXRow(Row row, List<String> header, FormulaEvaluator evaluator,
                                     NeighborhoodsMossoro neighborhoods,
                                     List<Notification> notificationBatch,
                                     List<NotificationWithError> errorBatch,
                                     Long currentIteration) {
        try {
            String[] csvLine = new String[header.size()];

            for (int i = 0; i < header.size(); i++) {
                Cell cell = row.getCell(i);
                csvLine[i] = getCellValueAsString(cell, evaluator);
            }

            String neighborhoodFromNotification = csvLine[header.indexOf("NM_BAIRRO")];

            if ((neighborhoodFromNotification = neighborhoods.search(neighborhoodFromNotification)) != null) {
                csvLine[header.indexOf("NM_BAIRRO")] = neighborhoodFromNotification;
                Notification notification = convertCsvLineToNotificationObject(csvLine, header);
                
                notificationBatch.add(notification);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar linha XLSX: " + e.getMessage());
        }
    }

    @Deprecated
    public static List<Notification> processXLSXFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            NeighborhoodsMossoro neighborhoods = new NeighborhoodsMossoro();
            List<Notification> notifications = new ArrayList<>();

            if (!rowIterator.hasNext()) {
                throw new IOException("O arquivo está vazio.");
            }

            Row headerRow = rowIterator.next();
            List<String> header = new ArrayList<>();
            for (Cell cell : headerRow) {
                header.add(cell.getStringCellValue());
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String[] csvLine = new String[header.size()];

                    for (int i = 0; i < header.size(); i++) {
                        Cell cell = row.getCell(i);
                        csvLine[i] = getCellValueAsString(cell, evaluator);
                    }

                    String neighborhoodFromNotification = csvLine[header.indexOf("NM_BAIRRO")];

                    if ((neighborhoodFromNotification = neighborhoods.search(neighborhoodFromNotification)) != null) {
                        csvLine[header.indexOf("NM_BAIRRO")] = neighborhoodFromNotification;
                        Notification notification = convertCsvLineToNotificationObject(csvLine, header);

                        notifications.add(notification);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return notifications;
        }
    }

    private static String getCellValueAsString(Cell cell, FormulaEvaluator evaluator) {
        try {
            if (cell == null) {
                return "";
            }
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        return dateFormat.format(cell.getDateCellValue());
                    } else {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    CellValue cellValue = evaluator.evaluate(cell);
                    switch (cellValue.getCellType()) {
                        case STRING:
                            return cellValue.getStringValue();
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                return dateFormat.format(cell.getDateCellValue());
                            } else {
                                return String.valueOf(cellValue.getNumberValue());
                            }
                        case BOOLEAN:
                            return String.valueOf(cellValue.getBooleanValue());
                        default:
                            return "";
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}
