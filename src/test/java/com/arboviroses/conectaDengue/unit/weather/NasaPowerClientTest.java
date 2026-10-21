package com.arboviroses.conectaDengue.Infrastructure.Weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NasaPowerClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NasaPowerClient client = new NasaPowerClient(null, null);

    @Test
    void mergesIsolatedParametersConvertsWindAndRejectsFillValue() throws Exception {
        Map<LocalDate, WeatherSample> samples = new LinkedHashMap<>();

        client.merge(samples, response("PRECTOTCORR", 12.21), "PRECTOTCORR");
        client.merge(samples, response("RH2M", -999), "RH2M");
        client.merge(samples, response("WS10M", 4.94), "WS10M");

        WeatherSample sample = samples.get(LocalDate.of(2025, 3, 1));
        assertEquals(12.21, sample.precipitationMm());
        assertNull(sample.relativeHumidityPct());
        assertEquals(17.784, sample.windSpeedKmh(), 0.0001);
    }

    private com.fasterxml.jackson.databind.JsonNode response(String parameter, double value) throws Exception {
        String json = """
            {"properties":{"parameter":{"%s":{"20250301":%s}}}}
            """.formatted(parameter, value);
        return objectMapper.readTree(json);
    }
}

