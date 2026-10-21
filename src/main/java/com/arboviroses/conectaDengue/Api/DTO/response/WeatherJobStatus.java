package com.arboviroses.conectaDengue.Api.DTO.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record WeatherJobStatus(
    String state,
    String mode,
    LocalDate startDate,
    LocalDate endDate,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt,
    int neighborhoods,
    int rowsProcessed,
    int completeRows,
    int partialRows,
    int failedRequests,
    int linkedNotifications,
    String message
) {
    public static WeatherJobStatus idle() {
        return new WeatherJobStatus("IDLE", null, null, null, null, null, 0, 0, 0, 0, 0, 0, null);
    }
}

