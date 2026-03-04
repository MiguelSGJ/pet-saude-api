package com.arboviroses.conectaDengue.Api.DTO;

public record EducacaoDTO(
        String ubs,
        Long eduCreche,
        Long eduPreEscola,
        Long eduAlfabetizacao,
        Long edu1a4,
        Long edu5a8,
        Long eduFundamentalCompleto,
        Long eduMedio,
        Long eduSuperior,
        Long eduNaoInformado,
        Long eduTotal) {
}
