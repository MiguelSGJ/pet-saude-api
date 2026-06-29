package com.arboviroses.conectaDengue.Api.DTO.request;

import com.arboviroses.conectaDengue.Api.Validation.InputPatterns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDTO {
    @NotBlank(message = "Refresh token nao pode ser vazio")
    @Size(max = 100, message = "Refresh token invalido")
    @Pattern(regexp = InputPatterns.REFRESH_TOKEN, message = "Refresh token invalido")
    private String token;
}
