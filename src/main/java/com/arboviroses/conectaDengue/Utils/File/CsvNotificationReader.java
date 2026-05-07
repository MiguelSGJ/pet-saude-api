package com.arboviroses.conectaDengue.Utils.File;

import com.arboviroses.conectaDengue.Api.DTO.request.NotificationDataDTO;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;

public class CsvNotificationReader {

    private static final Map<String, BiConsumer<NotificationDataDTO, String>> COLUMN_HANDLERS = new LinkedHashMap<>();

    static {
        COLUMN_HANDLERS.put("NU_NOTIFIC", (dto, v) -> dto.setNuNotific(parseLong(v)));
        COLUMN_HANDLERS.put("ID_AGRAVO",  NotificationDataDTO::setIdAgravo);
        COLUMN_HANDLERS.put("DT_SIN_PRI", NotificationDataDTO::setDtNotific);
        COLUMN_HANDLERS.put("DT_NASC",    NotificationDataDTO::setDtNasc);
        COLUMN_HANDLERS.put("CLASSI_FIN", NotificationDataDTO::setClassiFin);
        COLUMN_HANDLERS.put("CS_SEXO",    NotificationDataDTO::setCsSexo);
        COLUMN_HANDLERS.put("NM_BAIRRO",  NotificationDataDTO::setNmBairro);
        COLUMN_HANDLERS.put("ID_BAIRRO",  (dto, v) -> dto.setIdBairro(parseIntOrZero(v)));
        COLUMN_HANDLERS.put("EVOLUCAO",   NotificationDataDTO::setEvolucao);
        COLUMN_HANDLERS.put("NU_IDADE_N", (dto, v) -> dto.setIdade(parseIdadeSinan(v)));
    }

    private static char detectSeparator(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int read = inputStream.read(buffer);
        if (read <= 0) return ';';

        String firstLine = new String(buffer, 0, read, Charset.forName("ISO-8859-1"))
                .split("\n")[0];

        return Arrays.stream(new Character[]{';', ',', '\t', '|'})
                .max(Comparator.comparingLong(sep -> firstLine.chars().filter(c -> c == sep).count()))
                .orElse(';');
    }

    public static List<NotificationDataDTO> read(InputStream inputStream) throws IOException {
        List<NotificationDataDTO> result = new ArrayList<>();

        BufferedInputStream buffered = new BufferedInputStream(inputStream);
        buffered.mark(4096);
        char separator = detectSeparator(buffered);
        buffered.reset();

        InputStreamReader streamReader = new InputStreamReader(buffered, Charset.forName("ISO-8859-1"));

        try (CSVReader csvReader = new CSVReaderBuilder(streamReader)
                .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                .build()) {

            String[] header = csvReader.readNext();
            if (header == null) return result;

            Map<String, Integer> columnIndex = buildColumnIndex(header);

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                NotificationDataDTO dto = new NotificationDataDTO();
                final String[] currentRow = row;

                COLUMN_HANDLERS.forEach((colName, handler) -> {
                    Integer idx = columnIndex.get(colName);
                    if (idx == null || idx >= currentRow.length) return;

                    String value = currentRow[idx].trim();
                    if (!value.isEmpty()) {
                        handler.accept(dto, value);
                    }
                });

                result.add(dto);
            }
        }

        return result;
    }

    private static Map<String, Integer> buildColumnIndex(String[] header) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String name = header[i].trim();
            if (COLUMN_HANDLERS.containsKey(name)) {
                index.put(name, i);
            }
        }
        return index;
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
