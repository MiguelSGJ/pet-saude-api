package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Api.DTO.response.WeatherCoverageResponse;
import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Weather.WeatherDailyRepository;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeatherCoverageService {
    private final WeatherDailyRepository weatherDailyRepository;
    private final NotificationRepository notificationRepository;
    private final WeatherProperties properties;

    public WeatherCoverageService(
        WeatherDailyRepository weatherDailyRepository,
        NotificationRepository notificationRepository,
        WeatherProperties properties
    ) {
        this.weatherDailyRepository = weatherDailyRepository;
        this.notificationRepository = notificationRepository;
        this.properties = properties;
    }

    public WeatherCoverageResponse summarize(LocalDate start, LocalDate end) {
        List<WeatherDaily> rows = weatherDailyRepository.findByWeatherDateBetween(start, end);
        long precipitation = rows.stream().filter(row -> row.getPrecipitationMm() != null).count();
        long humidity = rows.stream().filter(row -> row.getRelativeHumidityPct() != null).count();
        long wind = rows.stream().filter(row -> row.getWindSpeedKmh() != null).count();
        long temperature = rows.stream().filter(row -> row.getTemperatureC() != null).count();
        long precipitationCompared = rows.stream()
            .filter(row -> row.getPrecipitationDivergent() != null).count();
        long precipitationDivergent = rows.stream()
            .filter(row -> Boolean.TRUE.equals(row.getPrecipitationDivergent())).count();
        long humidityCompared = rows.stream()
            .filter(row -> row.getHumidityDivergent() != null).count();
        long humidityDivergent = rows.stream()
            .filter(row -> Boolean.TRUE.equals(row.getHumidityDivergent())).count();
        long windCompared = rows.stream()
            .filter(row -> row.getWindDivergent() != null).count();
        long windDivergent = rows.stream()
            .filter(row -> Boolean.TRUE.equals(row.getWindDivergent())).count();
        long temperatureCompared = rows.stream()
            .filter(row -> row.getTemperatureDivergent() != null).count();
        long temperatureDivergent = rows.stream()
            .filter(row -> Boolean.TRUE.equals(row.getTemperatureDivergent())).count();
        long total = rows.size();
        return new WeatherCoverageResponse(
            start,
            end,
            total,
            precipitation,
            percentage(precipitation, total),
            humidity,
            percentage(humidity, total),
            wind,
            percentage(wind, total),
            temperature,
            percentage(temperature, total),
            precipitationCompared,
            precipitationDivergent,
            agreementPercentage(precipitationCompared, precipitationDivergent),
            humidityCompared,
            humidityDivergent,
            agreementPercentage(humidityCompared, humidityDivergent),
            windCompared,
            windDivergent,
            agreementPercentage(windCompared, windDivergent),
            temperatureCompared,
            temperatureDivergent,
            agreementPercentage(temperatureCompared, temperatureDivergent),
            properties.getMaxPrecipitationProviderDifferenceMm(),
            properties.getMaxHumidityProviderDifferencePctPoints(),
            properties.getMaxWindProviderDifferenceKmh(),
            properties.getMaxTemperatureProviderDifferenceC(),
            notificationRepository.countWeatherEligible(),
            notificationRepository.countWeatherLinked()
        );
    }

    private double percentage(long available, long total) {
        return total == 0 ? 0 : Math.round((available * 10000.0 / total)) / 100.0;
    }

    private double agreementPercentage(long compared, long divergent) {
        return compared == 0 ? 0 : percentage(compared - divergent, compared);
    }
}

