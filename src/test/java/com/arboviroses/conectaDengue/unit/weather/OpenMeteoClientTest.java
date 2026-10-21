package com.arboviroses.conectaDengue.Infrastructure.Weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpenMeteoClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenMeteoClient client = new OpenMeteoClient(null, null);

    @Test
    void parsesDailySeriesAndPreservesMissingValues() throws Exception {
        String json = """
            {
              "daily": {
                "time": ["2026-10-18", "2026-10-19"],
                "precipitation_sum": [1.2, null],
                "relative_humidity_2m_mean": [66, 70],
                "wind_speed_10m_mean": [13.6, 10.8]
              }
            }
            """;

        Map<LocalDate, WeatherSample> samples = client.parse(objectMapper.readTree(json));

        assertEquals(2, samples.size());
        assertEquals(1.2, samples.get(LocalDate.of(2026, 10, 18)).precipitationMm());
        assertEquals(66, samples.get(LocalDate.of(2026, 10, 18)).relativeHumidityPct());
        assertNull(samples.get(LocalDate.of(2026, 10, 19)).precipitationMm());
    }
}

