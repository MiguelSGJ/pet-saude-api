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
    long precipitationCompared,
    long precipitationDivergent,
    double precipitationAgreementPct,
    long humidityCompared,
    long humidityDivergent,
    double humidityAgreementPct,
    long windCompared,
    long windDivergent,
    double windAgreementPct,
    double precipitationDifferenceThresholdMm,
    double humidityDifferenceThresholdPctPoints,
    double windDifferenceThresholdKmh,
    long eligibleNotifications,
    long linkedNotifications
) {
}

