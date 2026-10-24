package com.arboviroses.conectaDengue.Infrastructure.Weather;

import java.time.LocalDate;

public record WeatherSample(
    LocalDate date,
    Double precipitationMm,
    Double relativeHumidityPct,
    Double windSpeedKmh
) {
    public static WeatherSample empty(LocalDate date) {
        return new WeatherSample(date, null, null, null);
    }

    public WeatherSample withPrecipitation(Double value) {
        return new WeatherSample(date, value, relativeHumidityPct, windSpeedKmh);
    }

    public WeatherSample withHumidity(Double value) {
        return new WeatherSample(date, precipitationMm, value, windSpeedKmh);
    }

    public WeatherSample withWind(Double value) {
        return new WeatherSample(date, precipitationMm, relativeHumidityPct, value);
    }
}

