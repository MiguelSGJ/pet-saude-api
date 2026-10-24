package com.arboviroses.conectaDengue.Domain.Repositories.Weather;

import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherDailyRepository extends JpaRepository<WeatherDaily, Long> {
    List<WeatherDaily> findByBairroIdAndWeatherDateBetween(
        Long bairroId,
        LocalDate start,
        LocalDate end
    );

    List<WeatherDaily> findByWeatherDateBetween(LocalDate start, LocalDate end);
}

