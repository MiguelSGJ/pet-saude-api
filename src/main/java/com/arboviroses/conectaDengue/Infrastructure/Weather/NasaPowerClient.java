package com.arboviroses.conectaDengue.Infrastructure.Weather;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NasaPowerClient implements WeatherProviderClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NasaPowerClient.class);
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

        // PRECTOTCORR pode retornar acumulados multiplicados por 24 quando uma
        // resposta atravessa fontes internas distintas (MERRA-2 e POWER/GEOS-IT).
        // A divisao usa o metadado oficial da resposta e evita correcao empirica.
        mergePrecipitation(samples, latitude, longitude, start, end);
        merge(samples, request(latitude, longitude, start, end, "RH2M"), "RH2M");
        merge(samples, request(latitude, longitude, start, end, "WS10M"), "WS10M");
        JsonNode temperature = request(latitude, longitude, start, end, "T2M,T2M_MIN,T2M_MAX");
        merge(samples, temperature, "T2M");
        merge(samples, temperature, "T2M_MIN");
        merge(samples, temperature, "T2M_MAX");
        return samples;
    }

    private void mergePrecipitation(
        Map<LocalDate, WeatherSample> samples,
        double latitude,
        double longitude,
        LocalDate start,
        LocalDate end
    ) {
        JsonNode response = request(latitude, longitude, start, end, "PRECTOTCORR");
        if (spansMultipleSources(response) && start.isBefore(end)) {
            long intervalDays = end.toEpochDay() - start.toEpochDay();
            LocalDate midpoint = start.plusDays(intervalDays / 2);
            LOGGER.info(
                "Resposta NASA PRECTOTCORR combina fontes no periodo {} a {}; subdividindo em {} a {} e {} a {}",
                start, end, start, midpoint, midpoint.plusDays(1), end
            );
            mergePrecipitation(samples, latitude, longitude, start, midpoint);
            mergePrecipitation(samples, latitude, longitude, midpoint.plusDays(1), end);
            return;
        }
        merge(samples, response, "PRECTOTCORR");
    }

    boolean spansMultipleSources(JsonNode root) {
        JsonNode sources = root.path("header").path("sources");
        return sources.isArray() && sources.size() > 1;
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
                case "T2M" -> current.withTemperature(value);
                case "T2M_MIN" -> current.withTemperatureMin(value);
                case "T2M_MAX" -> current.withTemperatureMax(value);
                default -> throw new IllegalArgumentException("Parametro NASA nao suportado: " + parameter);
            };
            samples.put(date, updated);
        });
    }
}

