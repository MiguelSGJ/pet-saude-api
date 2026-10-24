package com.arboviroses.conectaDengue.unit.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.arboviroses.conectaDengue.Api.DTO.response.WeatherCoverageResponse;
import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Weather.WeatherDailyRepository;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherCoverageService;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeatherCoverageServiceTest {
    @Test
    void summarizesCompletenessAndAgreementSeparately() {
        WeatherDailyRepository weatherRepository = mock(WeatherDailyRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        WeatherProperties properties = new WeatherProperties();
        LocalDate start = LocalDate.of(2026, 10, 18);
        LocalDate end = LocalDate.of(2026, 10, 19);

        WeatherDaily agreement = WeatherDaily.builder()
            .precipitationMm(2.0)
            .relativeHumidityPct(70.0)
            .windSpeedKmh(12.0)
            .precipitationDivergent(false)
            .humidityDivergent(false)
            .windDivergent(false)
            .build();
        WeatherDaily divergence = WeatherDaily.builder()
            .precipitationMm(20.0)
            .relativeHumidityPct(75.0)
            .windSpeedKmh(10.0)
            .precipitationDivergent(true)
            .humidityDivergent(false)
            .windDivergent(true)
            .build();
        when(weatherRepository.findByWeatherDateBetween(start, end)).thenReturn(List.of(agreement, divergence));
        when(notificationRepository.countWeatherEligible()).thenReturn(2L);
        when(notificationRepository.countWeatherLinked()).thenReturn(2L);

        WeatherCoverageResponse response = new WeatherCoverageService(
            weatherRepository, notificationRepository, properties
        ).summarize(start, end);

        assertEquals(100.0, response.precipitationCoveragePct());
        assertEquals(2, response.precipitationCompared());
        assertEquals(1, response.precipitationDivergent());
        assertEquals(50.0, response.precipitationAgreementPct());
        assertEquals(100.0, response.humidityAgreementPct());
        assertEquals(50.0, response.windAgreementPct());
        assertEquals(10.0, response.precipitationDifferenceThresholdMm());
        assertEquals(15.0, response.windDifferenceThresholdKmh());
    }
}
