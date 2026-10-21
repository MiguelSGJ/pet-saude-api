package com.arboviroses.conectaDengue.Infrastructure.Weather;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NasaPowerClient implements WeatherProviderClient {
    static final double FILL_VALUE = -999.0;
    private static final DateTimeFormatter NASA_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final WeatherApiRequester requester;
    private final WeatherProperties properties;

    public NasaPowerClient(WeatherApiRequester requester, WeatherProperties properties) {
        this.requester = requester;
        this.properties = properties;
    }

    @Override
    public WeatherProvider provider() {
        return WeatherProvider.NASA_POWER;
    }

    @Override
    public Map<LocalDate, WeatherSample> fetch(
        double latitude,
        double longitude,
        LocalDate start,
        LocalDate end
    ) {
        Map<LocalDate, WeatherSample> samples = new LinkedHashMap<>();

        // PRECTOTCORR apresentou valores inconsistentes quando solicitado junto
        // de outros parametros. As chamadas permanecem deliberadamente isoladas.
        merge(samples, request(latitude, longitude, start, end, "PRECTOTCORR"), "PRECTOTCORR");
        merge(samples, request(latitude, longitude, start, end, "RH2M"), "RH2M");
        merge(samples, request(latitude, longitude, start, end, "WS10M"), "WS10M");
        return samples;
    }

    private JsonNode request(
        double latitude,
        double longitude,
        LocalDate start,
        LocalDate end,
        String parameter
    ) {
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getNasaPowerBaseUrl())
            .queryParam("parameters", parameter)
            .queryParam("community", "AG")
            .queryParam("longitude", longitude)
            .queryParam("latitude", latitude)
            .queryParam("start", start.format(NASA_DATE))
            .queryParam("end", end.format(NASA_DATE))
            .queryParam("format", "JSON")
            .queryParam("time-standard", "UTC")
            .build()
            .encode()
            .toUri();
        return requester.get(uri);
    }

    void merge(Map<LocalDate, WeatherSample> samples, JsonNode root, String parameter) {
        JsonNode values = root.path("properties").path("parameter").path(parameter);
        if (!values.isObject()) {
            throw new IllegalArgumentException("Resposta NASA POWER sem parametro " + parameter);
        }

        values.fields().forEachRemaining(entry -> {
            LocalDate date = LocalDate.parse(entry.getKey(), NASA_DATE);
            double rawValue = entry.getValue().asDouble(FILL_VALUE);
            Double value = Double.compare(rawValue, FILL_VALUE) == 0 ? null : rawValue;
            WeatherSample current = samples.getOrDefault(date, WeatherSample.empty(date));
            WeatherSample updated = switch (parameter) {
                case "PRECTOTCORR" -> current.withPrecipitation(value);
                case "RH2M" -> current.withHumidity(value);
                case "WS10M" -> current.withWind(value == null ? null : value * 3.6);
                default -> throw new IllegalArgumentException("Parametro NASA nao suportado: " + parameter);
            };
            samples.put(date, updated);
        });
    }
}

