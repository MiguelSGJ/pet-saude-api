package com.arboviroses.conectaDengue.Api.DTO;

public record TratamentoDTO(
        String ubs,
        Long tratamentoFiltrada,
        Long tratamentoFervida,
        Long tratamentoClorada,
        Long tratamentoMineral,
        Long tratamentoSemTratamento,
        Long tratamentoNaoInformado,
        Long tratamentoTotal) {
}
