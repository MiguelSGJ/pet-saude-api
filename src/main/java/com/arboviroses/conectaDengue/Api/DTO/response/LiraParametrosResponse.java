package com.arboviroses.conectaDengue.Api.DTO.response;

public record LiraParametrosResponse(
        Integer ano,
        Integer liraNumber,
        Double limiteAlerta,
        Double limiteRisco
) {
}
