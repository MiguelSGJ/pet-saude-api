package com.arboviroses.conectaDengue.Api.DTO.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataNotificationInfoDTO 
{
    private long countTotal, countBySexoMasculino, countBySexoFeminino, countByEvolucao1, countByEvolucao2;
    private List<BairroCountDTO> listagemDeBairros;
}
