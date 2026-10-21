package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Api.DTO.response.WeatherCoverageResponse;
import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Weather.WeatherDailyRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeatherCoverageService {
    private final WeatherDailyRepository weatherDailyRepository;
    private final NotificationRepository notificationRepository;

    public WeatherCoverageService(
        WeatherDailyRepository weatherDailyRepository,
        NotificationRepository notificationRepository
    ) {
        this.weatherDailyRepository = weatherDailyRepository;
        this.notificationRepository = notificationRepository;
    }

    public WeatherCoverageResponse summarize(LocalDate start, LocalDate end) {
        List<WeatherDaily> rows = weatherDailyRepository.findByWeatherDateBetween(start, end);
        long precipitation = rows.stream().filter(row -> row.getPrecipitationMm() != null).count();
        long humidity = rows.stream().filter(row -> row.getRelativeHumidityPct() != null).count();
        long wind = rows.stream().filter(row -> row.getWindSpeedKmh() != null).count();
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
            notificationRepository.countWeatherEligible(),
            notificationRepository.countWeatherLinked()
        );
    }

    private double percentage(long available, long total) {
        return total == 0 ? 0 : Math.round((available * 10000.0 / total)) / 100.0;
    }
}

