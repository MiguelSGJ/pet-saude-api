package com.arboviroses.conectaDengue.Api.DTO.response;

import java.time.LocalDate;

public record WeatherCoverageResponse(
    LocalDate start,
    LocalDate end,
    long rows,
    long precipitationAvailable,
    double precipitationCoveragePct,
    long humidityAvailable,
    double humidityCoveragePct,
    long windAvailable,
    double windCoveragePct,
    long eligibleNotifications,
    long linkedNotifications
) {
}

