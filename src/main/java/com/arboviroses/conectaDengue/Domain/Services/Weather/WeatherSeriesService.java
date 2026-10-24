package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Api.DTO.response.WeatherGapsResponse;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Serie diaria da cidade (view weather_daily_city), calendario continuo do primeiro ao ultimo dia gravado.
 * Dias sem registro aparecem com valores nulos: nada e interpolado.
 */
@Service
public class WeatherSeriesService {
    public static final String DAILY_SERIES_SQL = """
        SELECT d.day AS weather_date,
               COALESCE(c.n_bairros, 0) AS n_bairros,
               c.temperature_c AS temperature_mean_c,
               c.temperature_min_c,
               c.temperature_max_c,
               c.precipitation_mm,
               c.relative_humidity_pct,
               c.wind_speed_kmh
        FROM (
            SELECT CAST(g AS date) AS day
            FROM generate_series(
                CAST((SELECT MIN(weather_date) FROM weather_daily) AS timestamp),
                CAST((SELECT MAX(weather_date) FROM weather_daily) AS timestamp),
                INTERVAL '1 day'
            ) g
        ) d
        LEFT JOIN weather_daily_city c ON c.weather_date = d.day
        ORDER BY d.day
        """;

    private static final String GAPS_SQL = """
        SELECT d.day AS weather_date,
               COALESCE(c.n_bairros, 0) AS n_bairros,
               c.weather_date IS NULL AS day_missing,
               COALESCE(c.null_temperature, 0) AS null_temperature,
               COALESCE(c.null_temperature_min, 0) AS null_temperature_min,
               COALESCE(c.null_temperature_max, 0) AS null_temperature_max,
               COALESCE(c.null_precipitation, 0) AS null_precipitation,
               COALESCE(c.null_humidity, 0) AS null_humidity,
               COALESCE(c.null_wind, 0) AS null_wind
        FROM (
            SELECT CAST(g AS date) AS day
            FROM generate_series(
                CAST((SELECT MIN(weather_date) FROM weather_daily) AS timestamp),
                CAST((SELECT MAX(weather_date) FROM weather_daily) AS timestamp),
                INTERVAL '1 day'
            ) g
        ) d
        LEFT JOIN weather_daily_city c ON c.weather_date = d.day
        ORDER BY d.day
        """;

    private final JdbcTemplate jdbcTemplate;

    public WeatherSeriesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WeatherGapsResponse gaps() {
        Date first = jdbcTemplate.queryForObject("SELECT MIN(weather_date) FROM weather_daily", Date.class);
        Date last = jdbcTemplate.queryForObject("SELECT MAX(weather_date) FROM weather_daily", Date.class);
        Integer expectedValue = jdbcTemplate.queryForObject("SELECT MAX(n_bairros) FROM weather_daily_city", Integer.class);
        int expected = expectedValue == null ? 0 : expectedValue;
        if (first == null || last == null) {
            return new WeatherGapsResponse(null, null, 0, expected, List.of());
        }

        List<WeatherGapsResponse.Gap> gaps = new ArrayList<>();
        jdbcTemplate.query(GAPS_SQL, rs -> {
            int neighborhoods = rs.getInt("n_bairros");
            List<String> missing = new ArrayList<>();
            if (rs.getBoolean("day_missing")) {
                missing.add("dia_sem_registro");
            } else {
                addIfMissing(missing, "temperature_mean_c", rs.getInt("null_temperature"));
                addIfMissing(missing, "temperature_min_c", rs.getInt("null_temperature_min"));
                addIfMissing(missing, "temperature_max_c", rs.getInt("null_temperature_max"));
                addIfMissing(missing, "precipitation_mm", rs.getInt("null_precipitation"));
                addIfMissing(missing, "relative_humidity_pct", rs.getInt("null_humidity"));
                addIfMissing(missing, "wind_speed_kmh", rs.getInt("null_wind"));
                if (neighborhoods < expected) {
                    missing.add("bairros_ausentes");
                }
            }
            if (!missing.isEmpty()) {
                gaps.add(new WeatherGapsResponse.Gap(rs.getDate("weather_date").toLocalDate(), neighborhoods, missing));
            }
        });

        LocalDate start = first.toLocalDate();
        LocalDate end = last.toLocalDate();
        return new WeatherGapsResponse(start, end, ChronoUnit.DAYS.between(start, end) + 1, expected, gaps);
    }

    private void addIfMissing(List<String> missing, String variable, int nullNeighborhoods) {
        if (nullNeighborhoods > 0) {
            missing.add(variable);
        }
    }
}
