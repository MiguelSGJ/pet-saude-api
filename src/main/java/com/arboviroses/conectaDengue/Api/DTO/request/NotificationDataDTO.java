package com.arboviroses.conectaDengue.Api.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDataDTO {
    @NotNull(message = "Número da notificação é obrigatório")
    private Long nuNotific;

    private String idAgravo;

    private String dtNotific;

    private String dtNasc;

    private String classiFin;

    private String csSexo;

    private String nmBairro;

    private Integer idBairro;

    private String evolucao;

    private Integer idade;
}
