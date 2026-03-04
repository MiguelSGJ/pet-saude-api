package com.arboviroses.conectaDengue.Api.DTO;

public record EscoamentoDTO(
        String ubs,
        Long escoamentoRedeColetora,
        Long escoamentoFossaSeptica,
        Long escoamentoFossaRudimentar,
        Long escoamentoRioMar,
        Long escoamentoCeuAberto,
        Long escoamentoOutra,
        Long escoamentoNaoInformado,
        Long escoamentoTotal) {
}
