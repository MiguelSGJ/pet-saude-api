package com.arboviroses.conectaDengue.Api.DTO.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDTO {
    @NotEmpty(message = "CPF não pode ser vazio")
    // @Size(min = 11, max = 14, message = "CPF deve ter 11 caracteres")
    private String cpf;

    @NotEmpty(message = "Senha não pode ser vazia")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    @NotEmpty(message = "Confirmação de senha não pode ser vazia")
    private String confirmPassword;

    @NotEmpty(message = "Nome completo não pode ser vazio")
    private String fullName;

    public RegisterUserDTO setCpf(String cpf) {
        System.out.println(cpf);

        this.cpf = cpf.replaceAll("\\D", "");
        return this;
    }
}