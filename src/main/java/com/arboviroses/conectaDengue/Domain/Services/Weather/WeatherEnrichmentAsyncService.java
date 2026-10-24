package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WeatherEnrichmentAsyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherEnrichmentAsyncService.class);

    private final WeatherEnrichmentService enrichmentService;
    private final NotificationRepository notificationRepository;
    private final WeatherJobMonitor monitor;
    private final WeatherJobLock jobLock;
    private final WeatherProperties properties;

    public WeatherEnrichmentAsyncService(
        WeatherEnrichmentService enrichmentService,
        NotificationRepository notificationRepository,
        WeatherJobMonitor monitor,
        WeatherJobLock jobLock,
        WeatherProperties properties
    ) {
        this.enrichmentService = enrichmentService;
        this.notificationRepository = notificationRepository;
        this.monitor = monitor;
        this.jobLock = jobLock;
        this.properties = properties;
    }

    @Async("weatherTaskExecutor")
    public void runFullBackfill() {
        LocalDate end = yesterday();
        LocalDate start = notificationRepository.findMinimumWeatherReferenceDate()
            .map(java.sql.Date::toLocalDate)
            .orElse(null);
        run("FULL_BACKFILL", start, end);
    }

    @Async("weatherTaskExecutor")
    public void runRecentReconciliation() {
        LocalDate end = yesterday();
        LocalDate start = end.minusDays(Math.max(0, properties.getReconciliationDays() - 1L));
        run("RECENT_RECONCILIATION", start, end);
    }

    @Async("weatherTaskExecutor")
    public void runRange(LocalDate start, LocalDate end) {
        run("CUSTOM_RANGE", start, end);
    }

    private void run(String mode, LocalDate start, LocalDate end) {
        if (!properties.isEnabled()) {
            monitor.skip(mode, "Enriquecimento meteorologico desabilitado");
            return;
        }
        if (start == null || end == null || start.isAfter(end)) {
            monitor.skip(mode, "Nenhuma data elegivel para processamento");
            return;
        }
        if (!jobLock.tryAcquire()) {
            monitor.skip(mode, "Outro job meteorologico esta em execucao");
            return;
        }

        OffsetDateTime startedAt = monitor.start(mode, start, end);
        try {
            WeatherJobReport report = enrichmentService.enrich(start, end);
            monitor.finish(mode, start, end, startedAt, report);
        } catch (Exception exception) {
            LOGGER.error("Job meteorologico {} falhou", mode, exception);
            monitor.fail(mode, start, end, startedAt, exception);
        } finally {
            jobLock.release();
        }
    }

    private LocalDate yesterday() {
        return LocalDate.now(ZoneId.of(properties.getTimezone())).minusDays(1);
    }
}

