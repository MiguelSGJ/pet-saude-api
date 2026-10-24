package com.arboviroses.conectaDengue.Api.Controllers;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherSeriesService;
import java.sql.ResultSetMetaData;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/weather/export")
public class WeatherExportController {
    private static final String NOTIFICATIONS_WITH_WEATHER_SQL = """
        SELECT n.id_notification, n.id_agravo, n.nome_bairro,
               CAST(n.data_notification AS date) AS data_notification,
               CAST(n.data_primeiro_sintoma AS date) AS data_primeiro_sintoma,
               n.semana_epidemiologica, n.classificacao, n.evolucao,
               n.weather_data_id, w.weather_date,
               w.precipitation_mm, w.relative_humidity_pct, w.wind_speed_kmh, w.temperature_c, w.temperature_min_c, w.temperature_max_c
        FROM notifications n
        LEFT JOIN weather_daily w ON w.id = n.weather_data_id
        ORDER BY COALESCE(n.data_primeiro_sintoma, n.data_notification), n.id_notification
        """;

    private final JdbcTemplate jdbcTemplate;

    public WeatherExportController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/notifications")
    public ResponseEntity<byte[]> notificationsWithWeather() throws IOException {
        return csv(NOTIFICATIONS_WITH_WEATHER_SQL, "notifications_com_clima.csv");
    }

    @GetMapping("/daily")
    public ResponseEntity<byte[]> dailyCitySeries() throws IOException {
        return csv(WeatherSeriesService.DAILY_SERIES_SQL, "clima_diario_cidade.csv");
    }

    // Resposta sincrona (sem StreamingResponseBody): o dispatch assincrono perde o JWT e o Spring Security nega acesso.
    private ResponseEntity<byte[]> csv(String sql, String filename) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.write(0xFEFF);
        jdbcTemplate.query(sql, rs -> {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                int columns = meta.getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    writer.write(escape(meta.getColumnLabel(i)));
                    writer.write(i < columns ? "," : "\n");
                }
                while (rs.next()) {
                    for (int i = 1; i <= columns; i++) {
                        Object value = rs.getObject(i);
                        writer.write(value == null ? "" : escape(value.toString()));
                        writer.write(i < columns ? "," : "\n");
                    }
                }
                return null;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        writer.flush();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(out.toByteArray());
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
