package com.arboviroses.conectaDengue.Infrastructure.Weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeatherApiRequester {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final WeatherProperties properties;

    public WeatherApiRequester(
        RestClient.Builder restClientBuilder,
        ObjectMapper objectMapper,
        WeatherProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMillis());
        requestFactory.setReadTimeout(properties.getReadTimeoutMillis());
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public JsonNode get(URI uri) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= properties.getMaxAttempts(); attempt++) {
            try {
                String body = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
                return objectMapper.readTree(body);
            } catch (Exception exception) {
                lastFailure = new IllegalStateException("Falha ao consultar API meteorologica: " + uri.getHost(), exception);
                if (attempt < properties.getMaxAttempts()) {
                    sleepBeforeRetry(attempt);
                }
            }
        }
        throw lastFailure;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(properties.getRetryBackoffMillis() * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Consulta meteorologica interrompida", exception);
        }
    }
}

