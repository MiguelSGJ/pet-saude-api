package com.arboviroses.conectaDengue.Infrastructure.Weather;

import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherEnrichmentAsyncService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeatherJobScheduler {
    private final WeatherEnrichmentAsyncService asyncService;
    private final WeatherProperties properties;

    public WeatherJobScheduler(
        WeatherEnrichmentAsyncService asyncService,
        WeatherProperties properties
    ) {
        this.asyncService = asyncService;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startBackfillWhenReady() {
        if (properties.isEnabled() && properties.isStartupBackfillEnabled()) {
            asyncService.runFullBackfill();
        }
    }

    @Scheduled(cron = "${weather.cron:0 30 4 * * *}", zone = "${weather.timezone:America/Fortaleza}")
    public void reconcileRecentDays() {
        if (properties.isEnabled()) {
            asyncService.runRecentReconciliation();
        }
    }
}

