package com.arboviroses.conectaDengue.Api.DTO.response;

import java.util.List;

public record LiraDisponibilidadeResponse(
        Integer ano,
        List<Integer> ciclos
) {
}
