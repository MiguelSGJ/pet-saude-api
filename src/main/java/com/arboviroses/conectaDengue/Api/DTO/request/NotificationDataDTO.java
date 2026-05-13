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

    /** DT_NOTIFIC — data de registro/notificação do caso. */
    private String dtNotific;

    /** DT_SIN_PRI — data de início dos primeiros sintomas. */
    private String dtSinPri;

    private String dtNasc;

    private String classiFin;

    private String csSexo;

    private String nmBairro;

    private Integer idBairro;

    private String evolucao;

    private Integer idade;
}
