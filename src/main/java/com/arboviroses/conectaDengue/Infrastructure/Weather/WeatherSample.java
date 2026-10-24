package com.arboviroses.conectaDengue.Infrastructure.Weather;

import java.time.LocalDate;

public record WeatherSample(
    LocalDate date,
    Double precipitationMm,
    Double relativeHumidityPct,
    Double windSpeedKmh,
    Double temperatureC,
    Double temperatureMinC,
    Double temperatureMaxC
) {
    public static WeatherSample empty(LocalDate date) {
        return new WeatherSample(date, null, null, null, null, null, null);
    }

    public WeatherSample withPrecipitation(Double value) {
        return new WeatherSample(date, value, relativeHumidityPct, windSpeedKmh, temperatureC, temperatureMinC, temperatureMaxC);
    }

    public WeatherSample withHumidity(Double value) {
        return new WeatherSample(date, precipitationMm, value, windSpeedKmh, temperatureC, temperatureMinC, temperatureMaxC);
    }

    public WeatherSample withWind(Double value) {
        return new WeatherSample(date, precipitationMm, relativeHumidityPct, value, temperatureC, temperatureMinC, temperatureMaxC);
    }

    public WeatherSample withTemperature(Double value) {
        return new WeatherSample(date, precipitationMm, relativeHumidityPct, windSpeedKmh, value, temperatureMinC, temperatureMaxC);
    }

    public WeatherSample withTemperatureMin(Double value) {
        return new WeatherSample(date, precipitationMm, relativeHumidityPct, windSpeedKmh, temperatureC, value, temperatureMaxC);
    }

    public WeatherSample withTemperatureMax(Double value) {
        return new WeatherSample(date, precipitationMm, relativeHumidityPct, windSpeedKmh, temperatureC, temperatureMinC, value);
    }
}

