package com.arboviroses.conectaDengue.Api.DTO;

public record AguaDTO(
        Integer ano,
        String ubs,
        String bairro,
        Long aguaRede,
        Long aguaPoco,
        Long aguaCisterna,
        Long aguaCarroPipe,
        Long aguaOutro,
        Long aguaNaoInformado,
        Long aguaTotal) {
}
