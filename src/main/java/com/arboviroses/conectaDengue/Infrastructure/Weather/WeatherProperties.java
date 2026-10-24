package com.arboviroses.conectaDengue.Infrastructure.Weather;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "weather")
@Getter
@Setter
public class WeatherProperties {
    private boolean enabled = true;
    private boolean startupBackfillEnabled = true;
    private String timezone = "America/Fortaleza";
    private String cron = "0 30 4 * * *";
    private int reconciliationDays = 7;
    private int batchDays = 366;
    private int connectTimeoutMillis = 10_000;
    private int readTimeoutMillis = 30_000;
    private int maxAttempts = 3;
    private long retryBackoffMillis = 1_000;
    private double maxDailyPrecipitationMm = 500;
    private double maxWindSpeedKmh = 300;
    private double maxPrecipitationProviderDifferenceMm = 10;
    private double maxHumidityProviderDifferencePctPoints = 10;
    private double maxWindProviderDifferenceKmh = 15;
    private long jobLockSeconds = 86_400;
    private String openMeteoBaseUrl = "https://archive-api.open-meteo.com/v1/archive";
    private String nasaPowerBaseUrl = "https://power.larc.nasa.gov/api/temporal/daily/point";
}

