package com.arboviroses.conectaDengue.Domain.Entities.Weather;

import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "weather_daily",
    uniqueConstraints = @UniqueConstraint(name = "uk_weather_daily_bairro_date", columnNames = {"bairro_id", "weather_date"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bairro_id", nullable = false)
    private Bairro bairro;

    private LocalDate weatherDate;
    private Double latitude;
    private Double longitude;

    private Double precipitationMm;
    private Double relativeHumidityPct;
    private Double windSpeedKmh;
    private String precipitationSource;
    private String humiditySource;
    private String windSource;

    private Double openMeteoPrecipitationMm;
    private Double openMeteoRelativeHumidityPct;
    private Double openMeteoWindSpeedKmh;
    private Double nasaPowerPrecipitationMm;
    private Double nasaPowerRelativeHumidityPct;
    private Double nasaPowerWindSpeedKmh;

    private Double precipitationProviderDifferenceMm;
    private Double humidityProviderDifferencePctPoints;
    private Double windProviderDifferenceKmh;
    private Boolean precipitationDivergent;
    private Boolean humidityDivergent;
    private Boolean windDivergent;
    private String comparisonStatus;

    private OffsetDateTime openMeteoFetchedAt;
    private OffsetDateTime nasaPowerFetchedAt;
    private String status;
    private String lastError;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

