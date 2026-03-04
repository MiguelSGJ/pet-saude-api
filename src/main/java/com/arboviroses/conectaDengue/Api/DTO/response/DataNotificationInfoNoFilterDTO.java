package com.arboviroses.conectaDengue.Api.DTO.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataNotificationInfoNoFilterDTO extends DataNotificationInfoDTO{
    private long countByIdAgravoDengue, countByIdAgravoZika, countByIdAgravoChikungunya;

    public DataNotificationInfoNoFilterDTO(
        long countTotal,
        long countBySexoMasculino,
        long countBySexoFeminino,
        long countByEvolucao1,
        long countByEvolucao2,
        List<BairroCountDTO> listagemDeBairros,
        long countByIdAgravoDengue, 
        long countByIdAgravoZika, 
        long countByIdAgravoChikungunya)
    {
        super(countTotal, countBySexoMasculino, countBySexoFeminino, countByEvolucao1, countByEvolucao2, listagemDeBairros);
        this.countByIdAgravoDengue = countByIdAgravoDengue;
        this.countByIdAgravoChikungunya = countByIdAgravoChikungunya;
        this.countByIdAgravoZika = countByIdAgravoZika;
    }
}
