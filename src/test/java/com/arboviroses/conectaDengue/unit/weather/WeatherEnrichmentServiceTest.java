package com.arboviroses.conectaDengue.unit.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import com.arboviroses.conectaDengue.Domain.Repositories.Bairros.BairroRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Weather.WeatherDailyRepository;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherEnrichmentService;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherJobReport;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherValueValidator;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProvider;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProviderClient;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherSample;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WeatherEnrichmentServiceTest {
    @Test
    void prefersOpenMeteoAndFallsBackToNasaPerVariable() {
        BairroRepository bairroRepository = mock(BairroRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        WeatherDailyRepository weatherRepository = mock(WeatherDailyRepository.class);
        WeatherProviderClient openMeteo = client(WeatherProvider.OPEN_METEO);
        WeatherProviderClient nasa = client(WeatherProvider.NASA_POWER);
        WeatherProperties properties = new WeatherProperties();
        LocalDate date = LocalDate.of(2026, 10, 19);

        Bairro bairro = new Bairro();
        bairro.setId(1L);
        bairro.setNome("CENTRO");
        bairro.setLatitude(-5.1920530);
        bairro.setLongitude(-37.3413511);
        when(bairroRepository.findAllReferencedByNotifications()).thenReturn(List.of(bairro));
        when(weatherRepository.findByBairroIdAndWeatherDateBetween(1L, date, date)).thenReturn(List.of());
        when(openMeteo.fetch(bairro.getLatitude(), bairro.getLongitude(), date, date)).thenReturn(Map.of(
            date, new WeatherSample(date, 2.0, null, 13.0, 28.4, 24.0, 33.0)
        ));
        when(nasa.fetch(bairro.getLatitude(), bairro.getLongitude(), date, date)).thenReturn(Map.of(
            date, new WeatherSample(date, 3.0, 65.0, 20.0, 30.0, 25.0, 35.0)
        ));
        when(notificationRepository.linkAvailableWeatherData()).thenReturn(4);

        WeatherEnrichmentService service = new WeatherEnrichmentService(
            bairroRepository,
            notificationRepository,
            weatherRepository,
            List.of(openMeteo, nasa),
            new WeatherValueValidator(properties),
            properties
        );

        WeatherJobReport report = service.enrich(date, date);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WeatherDaily>> captor = ArgumentCaptor.forClass(List.class);
        verify(weatherRepository).saveAll(captor.capture());
        WeatherDaily saved = captor.getValue().get(0);
        assertEquals(2.0, saved.getPrecipitationMm());
        assertEquals("OPEN_METEO", saved.getPrecipitationSource());
        assertEquals(65.0, saved.getRelativeHumidityPct());
        assertEquals("NASA_POWER", saved.getHumiditySource());
        assertEquals(13.0, saved.getWindSpeedKmh());
        assertEquals(1.0, saved.getPrecipitationProviderDifferenceMm());
        assertEquals(false, saved.getPrecipitationDivergent());
        assertEquals(7.0, saved.getWindProviderDifferenceKmh());
        assertEquals(false, saved.getWindDivergent());
        assertEquals(28.4, saved.getTemperatureC());
        assertEquals("OPEN_METEO", saved.getTemperatureSource());
        assertEquals(1.6, saved.getTemperatureProviderDifferenceC(), 1e-9);
        assertEquals(false, saved.getTemperatureDivergent());
        assertEquals(24.0, saved.getTemperatureMinC());
        assertEquals(33.0, saved.getTemperatureMaxC());
        assertEquals("AGREEMENT", saved.getComparisonStatus());
        assertEquals("COMPLETE", saved.getStatus());
        assertEquals(4, report.linkedNotifications());
    }

    @Test
    void flagsProviderDifferencesAboveConfiguredThresholds() {
        BairroRepository bairroRepository = mock(BairroRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        WeatherDailyRepository weatherRepository = mock(WeatherDailyRepository.class);
        WeatherProviderClient openMeteo = client(WeatherProvider.OPEN_METEO);
        WeatherProviderClient nasa = client(WeatherProvider.NASA_POWER);
        WeatherProperties properties = new WeatherProperties();
        LocalDate date = LocalDate.of(2026, 10, 19);

        Bairro bairro = new Bairro();
        bairro.setId(1L);
        bairro.setNome("CENTRO");
        bairro.setLatitude(-5.1920530);
        bairro.setLongitude(-37.3413511);
        when(bairroRepository.findAllReferencedByNotifications()).thenReturn(List.of(bairro));
        when(weatherRepository.findByBairroIdAndWeatherDateBetween(1L, date, date)).thenReturn(List.of());
        when(openMeteo.fetch(bairro.getLatitude(), bairro.getLongitude(), date, date)).thenReturn(Map.of(
            date, new WeatherSample(date, 2.0, 60.0, 10.0, 25.0, 20.0, 30.0)
        ));
        when(nasa.fetch(bairro.getLatitude(), bairro.getLongitude(), date, date)).thenReturn(Map.of(
            date, new WeatherSample(date, 15.0, 75.0, 30.0, 31.0, 26.0, 36.0)
        ));

        WeatherEnrichmentService service = new WeatherEnrichmentService(
            bairroRepository,
            notificationRepository,
            weatherRepository,
            List.of(openMeteo, nasa),
            new WeatherValueValidator(properties),
            properties
        );

        service.enrich(date, date);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WeatherDaily>> captor = ArgumentCaptor.forClass(List.class);
        verify(weatherRepository).saveAll(captor.capture());
        WeatherDaily saved = captor.getValue().get(0);
        assertEquals(true, saved.getPrecipitationDivergent());
        assertEquals(true, saved.getHumidityDivergent());
        assertEquals(true, saved.getWindDivergent());
        assertEquals(true, saved.getTemperatureDivergent());
        assertEquals("DIVERGENT", saved.getComparisonStatus());
        assertEquals(2.0, saved.getPrecipitationMm());
        assertEquals("OPEN_METEO", saved.getPrecipitationSource());
    }

    private WeatherProviderClient client(WeatherProvider provider) {
        WeatherProviderClient client = mock(WeatherProviderClient.class);
        when(client.provider()).thenReturn(provider);
        return client;
    }
}

