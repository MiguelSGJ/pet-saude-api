package com.arboviroses.conectaDengue.Infrastructure.Weather;

import java.time.LocalDate;
import java.util.Map;

public interface WeatherProviderClient {
    WeatherProvider provider();

    Map<LocalDate, WeatherSample> fetch(
        double latitude,
        double longitude,
        LocalDate start,
        LocalDate end
    );
}

