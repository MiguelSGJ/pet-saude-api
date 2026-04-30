package com.arboviroses.conectaDengue.Api.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NeighborhoodWeeklyPdfReportRequest {
    private Integer semanaInicial;
    private Integer semanaFinal;
    private Integer year;
    private String agravo;
    private String bairro;
}
