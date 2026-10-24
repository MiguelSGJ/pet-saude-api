package com.arboviroses.conectaDengue.Api.Controllers;

import com.arboviroses.conectaDengue.Api.DTO.response.SuccessResponseDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.WeatherCoverageResponse;
import com.arboviroses.conectaDengue.Api.DTO.response.WeatherJobStatus;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherCoverageService;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherEnrichmentAsyncService;
import com.arboviroses.conectaDengue.Domain.Services.Weather.WeatherJobMonitor;
import com.arboviroses.conectaDengue.Infrastructure.Weather.WeatherProperties;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/weather")
public class WeatherController {
    private final WeatherEnrichmentAsyncService asyncService;
    private final WeatherCoverageService coverageService;
    private final WeatherJobMonitor monitor;
    private final WeatherProperties properties;

    public WeatherController(
        WeatherEnrichmentAsyncService asyncService,
        WeatherCoverageService coverageService,
        WeatherJobMonitor monitor,
        WeatherProperties properties
    ) {
        this.asyncService = asyncService;
        this.coverageService = coverageService;
        this.monitor = monitor;
        this.properties = properties;
    }

    @PostMapping("/enrichment/backfill")
    public ResponseEntity<SuccessResponseDTO<String>> backfill() {
        asyncService.runFullBackfill();
        return ResponseEntity.accepted().body(SuccessResponseDTO.setResponse("Backfill meteorologico enfileirado", null));
    }

    @PostMapping("/enrichment/recent")
    public ResponseEntity<SuccessResponseDTO<String>> recent() {
        asyncService.runRecentReconciliation();
        return ResponseEntity.accepted().body(SuccessResponseDTO.setResponse("Reconciliacao meteorologica enfileirada", null));
    }

    @PostMapping("/enrichment/range")
    public ResponseEntity<SuccessResponseDTO<String>> range(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start deve ser anterior ou igual a end");
        }
        asyncService.runRange(start, end);
        return ResponseEntity.accepted().body(SuccessResponseDTO.setResponse("Periodo meteorologico enfileirado", null));
    }

    @GetMapping("/enrichment/status")
    public ResponseEntity<SuccessResponseDTO<WeatherJobStatus>> status() {
        return ResponseEntity.ok(SuccessResponseDTO.setResponse(monitor.get(), null));
    }

    @GetMapping("/coverage")
    public ResponseEntity<SuccessResponseDTO<WeatherCoverageResponse>> coverage(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        LocalDate effectiveEnd = end != null ? end : LocalDate.now(ZoneId.of(properties.getTimezone())).minusDays(1);
        LocalDate effectiveStart = start != null ? start : effectiveEnd.minusYears(2).plusDays(1);
        return ResponseEntity.ok(SuccessResponseDTO.setResponse(
            coverageService.summarize(effectiveStart, effectiveEnd), null
        ));
    }
}

