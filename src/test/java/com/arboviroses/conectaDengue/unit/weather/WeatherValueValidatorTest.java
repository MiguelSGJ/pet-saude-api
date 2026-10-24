package com.arboviroses.conectaDengue.unit.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherValueValidator;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherSample;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class WeatherValueValidatorTest {
    private final WeatherValueValidator validator = new WeatherValueValidator(new WeatherProperties());

    @Test
    void rejectsNonFiniteNegativeAndOutOfRangeValues() {
        WeatherSample sanitized = validator.sanitize(new WeatherSample(
            LocalDate.of(2026, 10, 20),
            -1.0,
            101.0,
            Double.NaN
        ));

        assertNull(sanitized.precipitationMm());
        assertNull(sanitized.relativeHumidityPct());
        assertNull(sanitized.windSpeedKmh());
    }

    @Test
    void acceptsBoundaryValues() {
        WeatherSample sanitized = validator.sanitize(new WeatherSample(
            LocalDate.of(2026, 10, 20),
            0.0,
            100.0,
            300.0
        ));

        assertEquals(0.0, sanitized.precipitationMm());
        assertEquals(100.0, sanitized.relativeHumidityPct());
        assertEquals(300.0, sanitized.windSpeedKmh());
    }
}

