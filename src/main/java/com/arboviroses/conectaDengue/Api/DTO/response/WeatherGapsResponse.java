package com.arboviroses.conectaDengue.Api.DTO.response;

import java.time.LocalDate;
import java.util.List;

public record WeatherGapsResponse(
    LocalDate start,
    LocalDate end,
    long calendarDays,
    int expectedNeighborhoods,
    List<Gap> gaps
) {
    public record Gap(LocalDate date, int neighborhoods, List<String> missing) {
    }
}
