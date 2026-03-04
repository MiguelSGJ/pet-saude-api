package com.arboviroses.conectaDengue.Api.DTO.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDTO {
    @NotEmpty(message = "CPF não pode ser vazio")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 caracteres")
    private String cpf;

    @NotEmpty(message = "Senha não pode ser vazia")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    public LoginUserDTO setCpf(String cpf)
    {
        this.cpf = cpf.replaceAll("\\D", "");

        return this;
    }
}
