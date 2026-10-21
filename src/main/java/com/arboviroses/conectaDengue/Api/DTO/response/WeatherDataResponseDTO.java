package com.arboviroses.conectaDengue.Api.DTO.response;

import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import java.time.LocalDate;

public record WeatherDataResponseDTO(
    LocalDate date,
    Double precipitationMm,
    Double relativeHumidityPct,
    Double windSpeedKmh,
    String precipitationSource,
    String humiditySource,
    String windSource,
    String status
) {
    public static WeatherDataResponseDTO from(WeatherDaily weather) {
        if (weather == null) {
            return null;
        }
        return new WeatherDataResponseDTO(
            weather.getWeatherDate(),
            weather.getPrecipitationMm(),
            weather.getRelativeHumidityPct(),
            weather.getWindSpeedKmh(),
            weather.getPrecipitationSource(),
            weather.getHumiditySource(),
            weather.getWindSource(),
            weather.getStatus()
        );
    }
}

