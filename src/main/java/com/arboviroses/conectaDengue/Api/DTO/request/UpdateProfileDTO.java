package com.arboviroses.conectaDengue.Api.DTO.request;

import com.arboviroses.conectaDengue.Api.Validation.InputPatterns;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileDTO {
    @NotEmpty(message = "CPF nao pode ser vazio")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 caracteres")
    @Pattern(regexp = InputPatterns.CPF, message = "CPF deve conter somente numeros")
    private String cpf;

    @NotEmpty(message = "Nome completo nao pode ser vazio")
    @Size(min = 3, max = 120, message = "Nome completo deve ter entre 3 e 120 caracteres")
    @Pattern(regexp = InputPatterns.SAFE_TEXT, message = "Nome completo deve conter apenas letras, numeros, espacos e pontuacao simples")
    private String fullName;

    @Pattern(regexp = InputPatterns.PASSWORD_OPTIONAL, message = "Senha atual nao pode conter caracteres de controle")
    private String currentPassword;

    @Pattern(regexp = InputPatterns.PASSWORD_OPTIONAL, message = "Nova senha nao pode conter caracteres de controle")
    private String newPassword;

    @Pattern(regexp = InputPatterns.PASSWORD_OPTIONAL, message = "Confirmacao da nova senha nao pode conter caracteres de controle")
    private String confirmNewPassword;

    public UpdateProfileDTO setCpf(String cpf) {
        this.cpf = cpf == null ? null : cpf.replaceAll("\\D", "");
        return this;
    }
}
