package com.arboviroses.conectaDengue.Api.DTO.request;

import com.arboviroses.conectaDengue.Api.Validation.InputPatterns;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDTO {
    @NotEmpty(message = "CPF nao pode ser vazio")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 caracteres")
    @Pattern(regexp = InputPatterns.CPF, message = "CPF deve conter somente numeros")
    private String cpf;

    @NotEmpty(message = "Senha nao pode ser vazia")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Size(max = 72, message = "Senha deve ter no maximo 72 caracteres")
    @Pattern(regexp = InputPatterns.PASSWORD, message = "Senha nao pode conter caracteres de controle")
    private String password;

    @NotEmpty(message = "Confirmacao de senha nao pode ser vazia")
    @Size(min = 6, max = 72, message = "Confirmacao de senha deve ter entre 6 e 72 caracteres")
    @Pattern(regexp = InputPatterns.PASSWORD, message = "Confirmacao de senha nao pode conter caracteres de controle")
    private String confirmPassword;

    @NotEmpty(message = "Nome completo nao pode ser vazio")
    @Size(min = 3, max = 120, message = "Nome completo deve ter entre 3 e 120 caracteres")
    @Pattern(regexp = InputPatterns.SAFE_TEXT, message = "Nome completo deve conter apenas letras, numeros, espacos e pontuacao simples")
    private String fullName;

    @Pattern(regexp = InputPatterns.USER_ROLE, message = "Tipo de acesso invalido")
    private String role;

    public RegisterUserDTO setCpf(String cpf) {
        this.cpf = cpf.replaceAll("\\D", "");
        return this;
    }
}
