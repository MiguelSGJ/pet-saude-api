package com.arboviroses.conectaDengue.Api.DTO;

public record RendaDTO(
        String ubs,
        Long rendaUmQuartoSalario,
        Long rendaMeioSalario,
        Long rendaUmSalario,
        Long rendaDoisSalarios,
        Long rendaTresSalarios,
        Long rendaQuatroSalarios,
        Long rendaAusencia,
        Long rendaAcimaQuatro,
        Long rendaNaoInformado,
        Long rendaTotal) {
}
