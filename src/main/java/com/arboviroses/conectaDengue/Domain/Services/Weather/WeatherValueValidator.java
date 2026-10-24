package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherSample;
import org.springframework.stereotype.Component;

@Component
public class WeatherValueValidator {
    private static final double MIN_TEMPERATURE_C = -90;
    private static final double MAX_TEMPERATURE_C = 60;

    private final WeatherProperties properties;

    public WeatherValueValidator(WeatherProperties properties) {
        this.properties = properties;
    }

    public WeatherSample sanitize(WeatherSample sample) {
        if (sample == null) {
            return null;
        }
        return new WeatherSample(
            sample.date(),
            inRange(sample.precipitationMm(), 0, properties.getMaxDailyPrecipitationMm()),
            inRange(sample.relativeHumidityPct(), 0, 100),
            inRange(sample.windSpeedKmh(), 0, properties.getMaxWindSpeedKmh()),
            inRange(sample.temperatureC(), MIN_TEMPERATURE_C, MAX_TEMPERATURE_C),
            inRange(sample.temperatureMinC(), MIN_TEMPERATURE_C, MAX_TEMPERATURE_C),
            inRange(sample.temperatureMaxC(), MIN_TEMPERATURE_C, MAX_TEMPERATURE_C)
        );
    }

    private Double inRange(Double value, double minimum, double maximum) {
        if (value == null || !Double.isFinite(value) || value < minimum || value > maximum) {
            return null;
        }
        return value;
    }
}

