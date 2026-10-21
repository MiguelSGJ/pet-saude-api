package com.arboviroses.conectaDengue.Domain.Services.Weather;

public record WeatherJobReport(
    int neighborhoods,
    int rowsProcessed,
    int completeRows,
    int partialRows,
    int failedRequests,
    int linkedNotifications
) {
}

