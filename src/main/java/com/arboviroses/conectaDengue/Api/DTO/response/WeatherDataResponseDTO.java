package com.arboviroses.conectaDengue.Api.DTO.response;

import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import java.time.LocalDate;

public record WeatherDataResponseDTO(
    LocalDate date,
    Double precipitationMm,
    Double relativeHumidityPct,
    Double windSpeedKmh,
    Double temperatureC,
    Double temperatureMinC,
    Double temperatureMaxC,
    String precipitationSource,
    String humiditySource,
    String windSource,
    String temperatureSource,
    Double precipitationProviderDifferenceMm,
    Double humidityProviderDifferencePctPoints,
    Double windProviderDifferenceKmh,
    Double temperatureProviderDifferenceC,
    Boolean precipitationDivergent,
    Boolean humidityDivergent,
    Boolean windDivergent,
    Boolean temperatureDivergent,
    String comparisonStatus,
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
            weather.getTemperatureC(),
            weather.getTemperatureMinC(),
            weather.getTemperatureMaxC(),
            weather.getPrecipitationSource(),
            weather.getHumiditySource(),
            weather.getWindSource(),
            weather.getTemperatureSource(),
            weather.getPrecipitationProviderDifferenceMm(),
            weather.getHumidityProviderDifferencePctPoints(),
            weather.getWindProviderDifferenceKmh(),
            weather.getTemperatureProviderDifferenceC(),
            weather.getPrecipitationDivergent(),
            weather.getHumidityDivergent(),
            weather.getWindDivergent(),
            weather.getTemperatureDivergent(),
            weather.getComparisonStatus(),
            weather.getStatus()
        );
    }
}

