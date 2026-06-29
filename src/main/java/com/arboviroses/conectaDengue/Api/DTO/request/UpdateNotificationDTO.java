package com.arboviroses.conectaDengue.Api.DTO.request;

import com.arboviroses.conectaDengue.Api.Validation.InputPatterns;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateNotificationDTO {
    @Size(max = 10, message = "Agravo deve ter no maximo 10 caracteres")
    @Pattern(regexp = InputPatterns.AGRAVO, message = "Agravo invalido")
    private String idAgravo;

    @Pattern(regexp = InputPatterns.DATE_BR, message = "Data de notificacao deve estar no formato dd/MM/yyyy")
    private String dtNotific;

    @Pattern(regexp = InputPatterns.DATE_BR, message = "Data de nascimento deve estar no formato dd/MM/yyyy")
    private String dtNasc;

    @Size(max = 20, message = "Classificacao deve ter no maximo 20 caracteres")
    @Pattern(regexp = InputPatterns.SAFE_CODE, message = "Classificacao invalida")
    private String classiFin;

    @Size(max = 20, message = "Sexo deve ter no maximo 20 caracteres")
    @Pattern(regexp = InputPatterns.SAFE_CODE, message = "Sexo invalido")
    private String csSexo;

    @Size(max = 120, message = "Bairro deve ter no maximo 120 caracteres")
    @Pattern(regexp = InputPatterns.SAFE_TEXT, message = "Bairro deve conter apenas letras, numeros, espacos e pontuacao simples")
    private String nmBairro;

    @Min(value = 0, message = "ID do bairro invalido")
    @Max(value = 999999, message = "ID do bairro invalido")
    private Integer idBairro;

    @Size(max = 20, message = "Evolucao deve ter no maximo 20 caracteres")
    @Pattern(regexp = InputPatterns.SAFE_CODE, message = "Evolucao invalida")
    private String evolucao;

    @Min(value = 0, message = "Idade invalida")
    @Max(value = 130, message = "Idade invalida")
    private Integer idade;
}
