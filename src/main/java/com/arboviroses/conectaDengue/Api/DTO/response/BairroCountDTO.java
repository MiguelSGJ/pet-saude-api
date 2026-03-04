package com.arboviroses.conectaDengue.Api.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BairroCountDTO
{
    private String nomeBairro;
    private long casosReportados;
    private long curados;
    private long mortePorAgravo;
    private long igorados;
}
