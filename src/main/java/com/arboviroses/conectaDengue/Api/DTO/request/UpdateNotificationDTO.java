package com.arboviroses.conectaDengue.Api.DTO.request;

import lombok.Data;

@Data
public class UpdateNotificationDTO {
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
