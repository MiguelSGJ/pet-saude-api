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
    long temperatureAvailable,
    double temperatureCoveragePct,
    long precipitationCompared,
    long precipitationDivergent,
    double precipitationAgreementPct,
    long humidityCompared,
    long humidityDivergent,
    double humidityAgreementPct,
    long windCompared,
    long windDivergent,
    double windAgreementPct,
    long temperatureCompared,
    long temperatureDivergent,
    double temperatureAgreementPct,
    double precipitationDifferenceThresholdMm,
    double humidityDifferenceThresholdPctPoints,
    double windDifferenceThresholdKmh,
    double temperatureDifferenceThresholdC,
    long eligibleNotifications,
    long linkedNotifications
) {
}

