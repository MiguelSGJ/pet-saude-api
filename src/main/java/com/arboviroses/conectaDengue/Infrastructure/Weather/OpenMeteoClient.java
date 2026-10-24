package com.arboviroses.conectaDengue.Infrastructure.Weather;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenMeteoClient implements WeatherProviderClient {
    private final WeatherApiRequester requester;
    private final WeatherProperties properties;

    public OpenMeteoClient(WeatherApiRequester requester, WeatherProperties properties) {
        this.requester = requester;
        this.properties = properties;
    }

    @Override
    public WeatherProvider provider() {
        return WeatherProvider.OPEN_METEO;
    }

    @Override
    public Map<LocalDate, WeatherSample> fetch(
        double latitude,
        double longitude,
        LocalDate start,
        LocalDate end
    ) {
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getOpenMeteoBaseUrl())
            .queryParam("latitude", latitude)
            .queryParam("longitude", longitude)
            .queryParam("start_date", start)
            .queryParam("end_date", end)
            .queryParam("daily", "precipitation_sum,relative_humidity_2m_mean,wind_speed_10m_mean,temperature_2m_mean,temperature_2m_min,temperature_2m_max")
            .queryParam("timezone", properties.getTimezone())
            .build()
            .encode()
            .toUri();

        return parse(requester.get(uri));
    }

    Map<LocalDate, WeatherSample> parse(JsonNode root) {
        JsonNode daily = root.path("daily");
        JsonNode dates = daily.path("time");
        JsonNode precipitation = daily.path("precipitation_sum");
        JsonNode humidity = daily.path("relative_humidity_2m_mean");
        JsonNode wind = daily.path("wind_speed_10m_mean");
        JsonNode temperature = daily.path("temperature_2m_mean");
        JsonNode temperatureMin = daily.path("temperature_2m_min");
        JsonNode temperatureMax = daily.path("temperature_2m_max");
        if (!dates.isArray()) {
            throw new IllegalArgumentException("Resposta Open-Meteo sem serie diaria");
        }

        Map<LocalDate, WeatherSample> samples = new LinkedHashMap<>();
        for (int index = 0; index < dates.size(); index++) {
            LocalDate date = LocalDate.parse(dates.get(index).asText());
            samples.put(date, new WeatherSample(
                date,
                nullableDouble(precipitation, index),
                nullableDouble(humidity, index),
                nullableDouble(wind, index),
                nullableDouble(temperature, index),
                nullableDouble(temperatureMin, index),
                nullableDouble(temperatureMax, index)
            ));
        }
        return samples;
    }

    private Double nullableDouble(JsonNode array, int index) {
        if (!array.isArray() || index >= array.size() || array.get(index).isNull()) {
            return null;
        }
        return array.get(index).asDouble();
    }
}

