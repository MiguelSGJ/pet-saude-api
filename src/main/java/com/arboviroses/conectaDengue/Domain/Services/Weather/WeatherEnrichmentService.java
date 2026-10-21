package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import com.arboviroses.conectaDengue.Domain.Repositories.Bairros.BairroRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Repositories.Weather.WeatherDailyRepository;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProvider;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProviderClient;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherSample;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WeatherEnrichmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherEnrichmentService.class);

    private final BairroRepository bairroRepository;
    private final NotificationRepository notificationRepository;
    private final WeatherDailyRepository weatherDailyRepository;
    private final Map<WeatherProvider, WeatherProviderClient> clients;
    private final WeatherValueValidator validator;
    private final WeatherProperties properties;

    public WeatherEnrichmentService(
        BairroRepository bairroRepository,
        NotificationRepository notificationRepository,
        WeatherDailyRepository weatherDailyRepository,
        List<WeatherProviderClient> providerClients,
        WeatherValueValidator validator,
        WeatherProperties properties
    ) {
        this.bairroRepository = bairroRepository;
        this.notificationRepository = notificationRepository;
        this.weatherDailyRepository = weatherDailyRepository;
        this.validator = validator;
        this.properties = properties;
        this.clients = providerClients.stream().collect(Collectors.toMap(
            WeatherProviderClient::provider,
            client -> client,
            (left, right) -> left,
            () -> new EnumMap<>(WeatherProvider.class)
        ));
    }

    public WeatherJobReport enrich(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return new WeatherJobReport(0, 0, 0, 0, 0, 0);
        }

        List<Bairro> neighborhoods = bairroRepository.findAllReferencedByNotifications();
        int rowsProcessed = 0;
        int completeRows = 0;
        int partialRows = 0;
        int failedRequests = 0;

        for (Bairro neighborhood : neighborhoods) {
            if (neighborhood.getLatitude() == null || neighborhood.getLongitude() == null) {
                LOGGER.warn("Bairro {} sem coordenadas; enriquecimento ignorado", neighborhood.getNome());
                failedRequests++;
                continue;
            }

            LocalDate batchStart = start;
            while (!batchStart.isAfter(end)) {
                LocalDate batchEnd = batchStart.plusDays(properties.getBatchDays() - 1L);
                if (batchEnd.isAfter(end)) {
                    batchEnd = end;
                }

                BatchResult result = enrichBatch(neighborhood, batchStart, batchEnd);
                rowsProcessed += result.rowsProcessed();
                completeRows += result.completeRows();
                partialRows += result.partialRows();
                failedRequests += result.failedRequests();
                batchStart = batchEnd.plusDays(1);
            }
        }

        int linkedNotifications = notificationRepository.linkAvailableWeatherData();
        LOGGER.info(
            "Enriquecimento meteorologico concluido: bairros={}, linhas={}, completas={}, parciais={}, falhas={}, notificacoesVinculadas={}",
            neighborhoods.size(), rowsProcessed, completeRows, partialRows, failedRequests, linkedNotifications
        );
        return new WeatherJobReport(
            neighborhoods.size(), rowsProcessed, completeRows, partialRows, failedRequests, linkedNotifications
        );
    }

    private BatchResult enrichBatch(Bairro neighborhood, LocalDate start, LocalDate end) {
        Map<WeatherProvider, Map<LocalDate, WeatherSample>> providerData = new EnumMap<>(WeatherProvider.class);
        List<String> errors = new ArrayList<>();
        int failedRequests = 0;

        for (WeatherProvider provider : WeatherProvider.values()) {
            WeatherProviderClient client = clients.get(provider);
            if (client == null) {
                errors.add(provider + ": cliente nao configurado");
                failedRequests++;
                continue;
            }
            try {
                providerData.put(provider, client.fetch(
                    neighborhood.getLatitude(), neighborhood.getLongitude(), start, end
                ));
            } catch (RuntimeException exception) {
                LOGGER.warn(
                    "Falha em {} para bairro {} no periodo {} a {}: {}",
                    provider, neighborhood.getNome(), start, end, exception.getMessage()
                );
                errors.add(provider + ": " + exception.getMessage());
                failedRequests++;
            }
        }

        Map<LocalDate, WeatherDaily> existing = weatherDailyRepository
            .findByBairroIdAndWeatherDateBetween(neighborhood.getId(), start, end)
            .stream()
            .collect(Collectors.toMap(WeatherDaily::getWeatherDate, row -> row));

        List<WeatherDaily> rows = new ArrayList<>();
        int complete = 0;
        int partial = 0;
        OffsetDateTime fetchedAt = OffsetDateTime.now();
        String errorMessage = truncate(String.join(" | ", errors), 1000);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            WeatherDaily row = existing.getOrDefault(date, newRow(neighborhood, date));
            row.setLatitude(neighborhood.getLatitude());
            row.setLongitude(neighborhood.getLongitude());

            WeatherSample openMeteo = sanitize(providerData, WeatherProvider.OPEN_METEO, date);
            WeatherSample nasaPower = sanitize(providerData, WeatherProvider.NASA_POWER, date);
            applyOpenMeteo(row, openMeteo, fetchedAt);
            applyNasaPower(row, nasaPower, fetchedAt);
            consolidate(row);
            compareProviders(row);
            row.setLastError(errorMessage.isBlank() ? null : errorMessage);
            row.setUpdatedAt(OffsetDateTime.now());

            if (isComplete(row)) {
                row.setStatus("COMPLETE");
                complete++;
            } else {
                row.setStatus("PARTIAL");
                partial++;
            }
            rows.add(row);
        }

        weatherDailyRepository.saveAll(rows);
        return new BatchResult(rows.size(), complete, partial, failedRequests);
    }

    private WeatherSample sanitize(
        Map<WeatherProvider, Map<LocalDate, WeatherSample>> providerData,
        WeatherProvider provider,
        LocalDate date
    ) {
        Map<LocalDate, WeatherSample> samples = providerData.get(provider);
        return samples == null ? null : validator.sanitize(samples.get(date));
    }

    private WeatherDaily newRow(Bairro neighborhood, LocalDate date) {
        OffsetDateTime now = OffsetDateTime.now();
        return WeatherDaily.builder()
            .bairro(neighborhood)
            .weatherDate(date)
            .latitude(neighborhood.getLatitude())
            .longitude(neighborhood.getLongitude())
            .status("PENDING")
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private void applyOpenMeteo(WeatherDaily row, WeatherSample sample, OffsetDateTime fetchedAt) {
        if (sample == null) {
            return;
        }
        if (sample.precipitationMm() != null) {
            row.setOpenMeteoPrecipitationMm(sample.precipitationMm());
        }
        if (sample.relativeHumidityPct() != null) {
            row.setOpenMeteoRelativeHumidityPct(sample.relativeHumidityPct());
        }
        if (sample.windSpeedKmh() != null) {
            row.setOpenMeteoWindSpeedKmh(sample.windSpeedKmh());
        }
        row.setOpenMeteoFetchedAt(fetchedAt);
    }

    private void applyNasaPower(WeatherDaily row, WeatherSample sample, OffsetDateTime fetchedAt) {
        if (sample == null) {
            return;
        }
        if (sample.precipitationMm() != null) {
            row.setNasaPowerPrecipitationMm(sample.precipitationMm());
        }
        if (sample.relativeHumidityPct() != null) {
            row.setNasaPowerRelativeHumidityPct(sample.relativeHumidityPct());
        }
        if (sample.windSpeedKmh() != null) {
            row.setNasaPowerWindSpeedKmh(sample.windSpeedKmh());
        }
        row.setNasaPowerFetchedAt(fetchedAt);
    }

    private void consolidate(WeatherDaily row) {
        row.setPrecipitationMm(first(row.getOpenMeteoPrecipitationMm(), row.getNasaPowerPrecipitationMm()));
        row.setRelativeHumidityPct(first(row.getOpenMeteoRelativeHumidityPct(), row.getNasaPowerRelativeHumidityPct()));
        row.setWindSpeedKmh(first(row.getOpenMeteoWindSpeedKmh(), row.getNasaPowerWindSpeedKmh()));
        row.setPrecipitationSource(source(row.getOpenMeteoPrecipitationMm(), row.getNasaPowerPrecipitationMm()));
        row.setHumiditySource(source(row.getOpenMeteoRelativeHumidityPct(), row.getNasaPowerRelativeHumidityPct()));
        row.setWindSource(source(row.getOpenMeteoWindSpeedKmh(), row.getNasaPowerWindSpeedKmh()));
    }

    private void compareProviders(WeatherDaily row) {
        row.setPrecipitationProviderDifferenceMm(absoluteDifference(
            row.getOpenMeteoPrecipitationMm(), row.getNasaPowerPrecipitationMm()
        ));
        row.setHumidityProviderDifferencePctPoints(absoluteDifference(
            row.getOpenMeteoRelativeHumidityPct(), row.getNasaPowerRelativeHumidityPct()
        ));
        row.setWindProviderDifferenceKmh(absoluteDifference(
            row.getOpenMeteoWindSpeedKmh(), row.getNasaPowerWindSpeedKmh()
        ));

        row.setPrecipitationDivergent(exceeds(
            row.getPrecipitationProviderDifferenceMm(), properties.getMaxPrecipitationProviderDifferenceMm()
        ));
        row.setHumidityDivergent(exceeds(
            row.getHumidityProviderDifferencePctPoints(), properties.getMaxHumidityProviderDifferencePctPoints()
        ));
        row.setWindDivergent(exceeds(
            row.getWindProviderDifferenceKmh(), properties.getMaxWindProviderDifferenceKmh()
        ));

        boolean hasComparison = row.getPrecipitationDivergent() != null
            || row.getHumidityDivergent() != null
            || row.getWindDivergent() != null;
        boolean hasDivergence = Boolean.TRUE.equals(row.getPrecipitationDivergent())
            || Boolean.TRUE.equals(row.getHumidityDivergent())
            || Boolean.TRUE.equals(row.getWindDivergent());
        row.setComparisonStatus(hasDivergence ? "DIVERGENT" : hasComparison ? "AGREEMENT" : "UNAVAILABLE");
    }

    private Double absoluteDifference(Double left, Double right) {
        return left == null || right == null ? null : Math.abs(left - right);
    }

    private Boolean exceeds(Double difference, double threshold) {
        return difference == null ? null : difference > threshold;
    }

    private Double first(Double primary, Double secondary) {
        return primary != null ? primary : secondary;
    }

    private String source(Double primary, Double secondary) {
        if (primary != null) {
            return WeatherProvider.OPEN_METEO.name();
        }
        return secondary != null ? WeatherProvider.NASA_POWER.name() : null;
    }

    private boolean isComplete(WeatherDaily row) {
        return row.getPrecipitationMm() != null
            && row.getRelativeHumidityPct() != null
            && row.getWindSpeedKmh() != null;
    }

    private String truncate(String value, int maximumLength) {
        if (value == null || value.length() <= maximumLength) {
            return Objects.requireNonNullElse(value, "");
        }
        return value.substring(0, maximumLength);
    }

    private record BatchResult(int rowsProcessed, int completeRows, int partialRows, int failedRequests) {
    }
}

