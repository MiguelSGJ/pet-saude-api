package com.arboviroses.conectaDengue.Api.DTO.request;

import com.arboviroses.conectaDengue.Api.Validation.InputPatterns;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NeighborhoodWeeklyPdfReportRequest {
    @Min(value = 1, message = "Semana inicial invalida")
    @Max(value = 53, message = "Semana inicial invalida")
    private Integer semanaInicial;

    @Min(value = 1, message = "Semana final invalida")
    @Max(value = 53, message = "Semana final invalida")
    private Integer semanaFinal;

    @Min(value = 2000, message = "Ano invalido")
    @Max(value = 2100, message = "Ano invalido")
    private Integer year;

    @Size(max = 30, message = "Agravo deve ter no maximo 30 caracteres")
    @Pattern(regexp = InputPatterns.AGRAVO, message = "Agravo invalido")
    private String agravo;

    @Size(max = 120, message = "Bairro deve ter no maximo 120 caracteres")
    @Pattern(regexp = InputPatterns.SAFE_TEXT, message = "Bairro deve conter apenas letras, numeros, espacos e pontuacao simples")
    private String bairro;

    @Pattern(regexp = InputPatterns.SCOPE, message = "Escopo invalido")
    private String scope;
}
