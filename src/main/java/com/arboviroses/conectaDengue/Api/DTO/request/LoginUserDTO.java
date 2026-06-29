package com.arboviroses.conectaDengue.Api.DTO.request;

import com.arboviroses.conectaDengue.Api.Validation.InputPatterns;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDTO {
    @NotEmpty(message = "CPF nao pode ser vazio")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 caracteres")
    @Pattern(regexp = InputPatterns.CPF, message = "CPF deve conter somente numeros")
    private String cpf;

    @NotEmpty(message = "Senha nao pode ser vazia")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Size(max = 72, message = "Senha deve ter no maximo 72 caracteres")
    @Pattern(regexp = InputPatterns.PASSWORD, message = "Senha nao pode conter caracteres de controle")
    private String password;

    public LoginUserDTO setCpf(String cpf)
    {
        this.cpf = cpf.replaceAll("\\D", "");

        return this;
    }
}
