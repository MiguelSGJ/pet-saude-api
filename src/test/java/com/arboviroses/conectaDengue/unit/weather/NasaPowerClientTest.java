package com.arboviroses.conectaDengue.Infrastructure.Weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
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
        client.merge(samples, response("T2M", 27.35), "T2M");
        client.merge(samples, response("T2M_MIN", 22.5), "T2M_MIN");
        client.merge(samples, response("T2M_MAX", 34.1), "T2M_MAX");

        WeatherSample sample = samples.get(LocalDate.of(2025, 3, 1));
        assertEquals(12.21, sample.precipitationMm());
        assertNull(sample.relativeHumidityPct());
        assertEquals(17.784, sample.windSpeedKmh(), 0.0001);
        assertEquals(27.35, sample.temperatureC());
        assertEquals(22.5, sample.temperatureMinC());
        assertEquals(34.1, sample.temperatureMaxC());
    }

    @Test
    void detectsResponsesThatSpanMultipleInternalSources() throws Exception {
        var mixed = objectMapper.readTree("""
            {"header":{"sources":["MERRA2","POWER"]}}
            """);
        var single = objectMapper.readTree("""
            {"header":{"sources":["MERRA2"]}}
            """);

        assertEquals(true, client.spansMultipleSources(mixed));
        assertEquals(false, client.spansMultipleSources(single));
    }

    @Test
    void splitsMixedSourcePrecipitationInsteadOfUsingInflatedValues() throws Exception {
        WeatherApiRequester requester = mock(WeatherApiRequester.class);
        NasaPowerClient splittingClient = new NasaPowerClient(requester, new WeatherProperties());
        when(requester.get(any(URI.class))).thenAnswer(invocation -> {
            String query = invocation.getArgument(0, URI.class).getQuery();
            if (query.contains("parameters=PRECTOTCORR") && query.contains("start=20260301")
                && query.contains("end=20260302")) {
                return objectMapper.readTree("""
                    {"header":{"sources":["MERRA2","POWER"]},"properties":{"parameter":{"PRECTOTCORR":{"20260301":240,"20260302":480}}}}
                    """);
            }
            if (query.contains("parameters=PRECTOTCORR") && query.contains("start=20260301")) {
                return responseWithSource("PRECTOTCORR", "MERRA2", "20260301", 10.0);
            }
            if (query.contains("parameters=PRECTOTCORR") && query.contains("start=20260302")) {
                return responseWithSource("PRECTOTCORR", "POWER", "20260302", 20.0);
            }
            if (query.contains("parameters=RH2M")) {
                return objectMapper.readTree("""
                    {"properties":{"parameter":{"RH2M":{"20260301":70,"20260302":75}}}}
                    """);
            }
            if (query.contains("parameters=T2M")) {
                return objectMapper.readTree("""
                    {"properties":{"parameter":{"T2M":{"20260301":26.5,"20260302":27.5},"T2M_MIN":{"20260301":22.0,"20260302":23.0},"T2M_MAX":{"20260301":33.0,"20260302":34.0}}}}
                    """);
            }
            return objectMapper.readTree("""
                {"properties":{"parameter":{"WS10M":{"20260301":2,"20260302":3}}}}
                """);
        });

        Map<LocalDate, WeatherSample> samples = splittingClient.fetch(
            -5.1875, -37.3442, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)
        );

        assertEquals(10.0, samples.get(LocalDate.of(2026, 3, 1)).precipitationMm());
        assertEquals(20.0, samples.get(LocalDate.of(2026, 3, 2)).precipitationMm());
        assertEquals(26.5, samples.get(LocalDate.of(2026, 3, 1)).temperatureC());
        assertEquals(22.0, samples.get(LocalDate.of(2026, 3, 1)).temperatureMinC());
        assertEquals(34.0, samples.get(LocalDate.of(2026, 3, 2)).temperatureMaxC());
        verify(requester, times(6)).get(any(URI.class));
    }

    private com.fasterxml.jackson.databind.JsonNode response(String parameter, double value) throws Exception {
        String json = """
            {"properties":{"parameter":{"%s":{"20250301":%s}}}}
            """.formatted(parameter, value);
        return objectMapper.readTree(json);
    }

    private com.fasterxml.jackson.databind.JsonNode responseWithSource(
        String parameter,
        String source,
        String date,
        double value
    ) throws Exception {
        String json = """
            {"header":{"sources":["%s"]},"properties":{"parameter":{"%s":{"%s":%s}}}}
            """.formatted(source, parameter, date, value);
        return objectMapper.readTree(json);
    }
}

