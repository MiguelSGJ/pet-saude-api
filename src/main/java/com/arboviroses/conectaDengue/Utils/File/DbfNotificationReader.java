package com.arboviroses.conectaDengue.Utils.File;

import com.arboviroses.conectaDengue.Api.DTO.request.NotificationDataDTO;
import com.linuxense.javadbf.DBFReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class DbfNotificationReader {

    private static final Logger log = Logger.getLogger(DbfNotificationReader.class.getName());

    private static final Set<String> DATE_COLUMNS = Set.of("DT_NOTIFIC", "DT_SIN_PRI", "DT_NASC");

    private static final Map<String, BiConsumer<NotificationDataDTO, String>> COLUMN_HANDLERS = new LinkedHashMap<>();

    static {
        COLUMN_HANDLERS.put("NU_NOTIFIC", (dto, v) -> dto.setNuNotific(parseLong(v)));
        COLUMN_HANDLERS.put("ID_AGRAVO",  NotificationDataDTO::setIdAgravo);
        COLUMN_HANDLERS.put("DT_NOTIFIC", NotificationDataDTO::setDtNotific);
        COLUMN_HANDLERS.put("DT_SIN_PRI", NotificationDataDTO::setDtSinPri);
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
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");

        try (DBFReader reader = new DBFReader(inputStream, Charset.forName("ISO-8859-1"))) {
            Map<String, Integer> fieldIndex = buildFieldIndex(reader);

            Object[] row;
            while ((row = reader.nextRecord()) != null) {
                NotificationDataDTO dto = new NotificationDataDTO();
                final Object[] currentRow = row;

                COLUMN_HANDLERS.forEach((colName, handler) -> {
                    Integer idx = fieldIndex.get(colName);
                    if (idx == null || currentRow[idx] == null) return;

                    String value;
                    if (DATE_COLUMNS.contains(colName) && currentRow[idx] instanceof Date date) {
                        value = fmt.format(date);
                    } else {
                        value = objectToString(currentRow[idx]);
                    }

                    if (!value.isBlank()) {
                        handler.accept(dto, value.trim());
                    }
                });

                result.add(dto);
            }
        }

        return result;
    }

    private static Map<String, Integer> buildFieldIndex(DBFReader reader) {
        Map<String, Integer> index = new HashMap<>();
        List<String> allFields = new ArrayList<>();
        for (int i = 0; i < reader.getFieldCount(); i++) {
            String name = reader.getField(i).getName().trim();
            allFields.add(name);
            if (COLUMN_HANDLERS.containsKey(name)) {
                index.put(name, i);
            }
        }
        log.info("[DbfNotificationReader] campos no arquivo: " + allFields);
        log.info("[DbfNotificationReader] campos mapeados: " + index.keySet());
        return index;
    }

    private static String objectToString(Object value) {
        if (value instanceof Double d) {
            return d == Math.floor(d) ? String.valueOf(d.longValue()) : String.valueOf(d);
        }
        return value.toString().trim();
    }

    private static int parseIdadeSinan(String v) {
        try {
            int raw = Integer.parseInt(v.split("\\.")[0]);
            int unit = raw / 1000;
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
