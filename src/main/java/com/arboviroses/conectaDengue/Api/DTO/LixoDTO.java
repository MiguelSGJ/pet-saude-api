package com.arboviroses.conectaDengue.Api.DTO;

public record LixoDTO(
        String ubs,
        Long lixoColetado,
        Long lixoQueimadoEnterrado,
        Long lixoCeuAberto,
        Long lixoOutro,
        Long lixoNaoInformado,
        Long lixoTotal) {
}
