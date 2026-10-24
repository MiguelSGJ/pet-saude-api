package com.arboviroses.conectaDengue.Domain.Entities.Weather;

import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import jakarta.persistence.Column;
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
    @Column(name = "temperature_c")
    private Double temperatureC;
    @Column(name = "temperature_min_c")
    private Double temperatureMinC;
    @Column(name = "temperature_max_c")
    private Double temperatureMaxC;
    private String precipitationSource;
    private String humiditySource;
    private String windSource;
    private String temperatureSource;

    private Double openMeteoPrecipitationMm;
    private Double openMeteoRelativeHumidityPct;
    private Double openMeteoWindSpeedKmh;
    private Double nasaPowerPrecipitationMm;
    private Double nasaPowerRelativeHumidityPct;
    private Double nasaPowerWindSpeedKmh;
    @Column(name = "open_meteo_temperature_c")
    private Double openMeteoTemperatureC;
    @Column(name = "open_meteo_temperature_min_c")
    private Double openMeteoTemperatureMinC;
    @Column(name = "open_meteo_temperature_max_c")
    private Double openMeteoTemperatureMaxC;
    @Column(name = "nasa_power_temperature_c")
    private Double nasaPowerTemperatureC;
    @Column(name = "nasa_power_temperature_min_c")
    private Double nasaPowerTemperatureMinC;
    @Column(name = "nasa_power_temperature_max_c")
    private Double nasaPowerTemperatureMaxC;

    private Double precipitationProviderDifferenceMm;
    private Double humidityProviderDifferencePctPoints;
    private Double windProviderDifferenceKmh;
    @Column(name = "temperature_provider_difference_c")
    private Double temperatureProviderDifferenceC;
    private Boolean precipitationDivergent;
    private Boolean humidityDivergent;
    private Boolean windDivergent;
    private Boolean temperatureDivergent;
    private String comparisonStatus;

    private OffsetDateTime openMeteoFetchedAt;
    private OffsetDateTime nasaPowerFetchedAt;
    private String status;
    private String lastError;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

