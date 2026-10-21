package com.arboviroses.conectaDengue.Domain.Services.Weather;

import com.arboviroses.conectaDengue.Api.DTO.response.WeatherJobStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class WeatherJobMonitor {
    private final AtomicReference<WeatherJobStatus> status = new AtomicReference<>(WeatherJobStatus.idle());

    public WeatherJobStatus get() {
        return status.get();
    }

    public OffsetDateTime start(String mode, LocalDate start, LocalDate end) {
        OffsetDateTime startedAt = OffsetDateTime.now();
        status.set(new WeatherJobStatus(
            "RUNNING", mode, start, end, startedAt, null,
            0, 0, 0, 0, 0, 0, null
        ));
        return startedAt;
    }

    public void finish(String mode, LocalDate start, LocalDate end, OffsetDateTime startedAt, WeatherJobReport report) {
        status.set(new WeatherJobStatus(
            "COMPLETED", mode, start, end, startedAt, OffsetDateTime.now(),
            report.neighborhoods(), report.rowsProcessed(), report.completeRows(),
            report.partialRows(), report.failedRequests(), report.linkedNotifications(), null
        ));
    }

    public void fail(String mode, LocalDate start, LocalDate end, OffsetDateTime startedAt, Exception exception) {
        status.set(new WeatherJobStatus(
            "FAILED", mode, start, end, startedAt, OffsetDateTime.now(),
            0, 0, 0, 0, 0, 0, exception.getMessage()
        ));
    }

    public void skip(String mode, String message) {
        status.set(new WeatherJobStatus(
            "SKIPPED", mode, null, null, null, OffsetDateTime.now(),
            0, 0, 0, 0, 0, 0, message
        ));
    }
}

