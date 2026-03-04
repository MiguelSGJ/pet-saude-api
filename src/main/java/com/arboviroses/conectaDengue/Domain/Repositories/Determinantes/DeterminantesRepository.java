package com.arboviroses.conectaDengue.Domain.Repositories.Determinantes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.arboviroses.conectaDengue.Api.DTO.AguaDTO;
import com.arboviroses.conectaDengue.Api.DTO.EducacaoDTO;
import com.arboviroses.conectaDengue.Api.DTO.EscoamentoDTO;
import com.arboviroses.conectaDengue.Api.DTO.LixoDTO;
import com.arboviroses.conectaDengue.Api.DTO.RendaDTO;
import com.arboviroses.conectaDengue.Api.DTO.TratamentoDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Determinantes;

@Repository
public interface DeterminantesRepository extends JpaRepository<Determinantes, Long> {
    
    void deleteByAno(Integer ano);

    List<Determinantes> findByAnoOrderByUbsAsc(Integer ano);

    List<Determinantes> findAllByOrderByAnoDescUbsAsc();

    List<Determinantes> findByUbsContainingIgnoreCaseOrderByAnoDesc(String ubs);

    List<Determinantes> findByBairroContainingIgnoreCaseOrderByAnoDescUbsAsc(String bairro);


    @Query(""" 
        SELECT new com.api.determinantes.sociais.Api.DTO.AguaDTO(
            d.ano, 
            d.ubs, 
            d.bairro, 
        CAST(d.aguaRede AS long), 
        CAST(d.aguaPoco AS long), 
        CAST(d.aguaCisterna AS long), 
        CAST(d.aguaCarroPipe AS long), 
        CAST(d.aguaOutro AS long), 
        CAST(d.aguaNaoInformado AS long), 
        CAST(d.aguaTotal AS long)) 
        FROM Determinantes d 
        ORDER BY d.ano DESC, d.ubs ASC 
        """
    )
    List<AguaDTO> findAllAgua();

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.AguaDTO(
            d.ano, 
            d.ubs, 
            d.bairro,
        CAST(d.aguaRede AS long),
        CAST(d.aguaPoco AS long), 
        CAST(d.aguaCisterna AS long),
        CAST(d.aguaCarroPipe AS long), 
        CAST(d.aguaOutro AS long), 
        CAST(d.aguaNaoInformado AS long), 
        CAST(d.aguaTotal AS long)) 
        FROM Determinantes d 
        WHERE UPPER(d.ubs) LIKE UPPER(CONCAT('%', :ubs, '%')) 
        ORDER BY d.ano DESC
        """
    )
    List<AguaDTO> findAguaByUbs(String ubs);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.AguaDTO(
            d.ano, 
            'AGREGADO', 
            d.bairro, 
        SUM(d.aguaRede), 
        SUM(d.aguaPoco), 
        SUM(d.aguaCisterna), 
        SUM(d.aguaCarroPipe), 
        SUM(d.aguaOutro), 
        SUM(d.aguaNaoInformado), 
        SUM(d.aguaTotal)) 
        FROM Determinantes d 
        WHERE UPPER(d.bairro) LIKE UPPER(CONCAT('%', :bairro, '%')) 
        GROUP BY d.ano, d.bairro 
        ORDER BY d.ano DESC
        """
    )
    List<AguaDTO> aggregateAguaByBairro(String bairro);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.TratamentoDTO(
            d.ubs, 
        CAST(d.tratamentoFiltrada AS long), 
        CAST(d.tratamentoFervida AS long), 
        CAST(d.tratamentoClorada AS long), 
        CAST(d.tratamentoMineral AS long), 
        CAST(d.tratamentoSemTratamento AS long), 
        CAST(d.tratamentoNaoInformado AS long), 
        CAST(d.tratamentoTotal AS long)) 
        FROM Determinantes d 
        ORDER BY d.ano DESC, d.ubs ASC
        """
    )
    List<TratamentoDTO> findAllTratamento();

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.TratamentoDTO(
            d.ubs, 
        CAST(d.tratamentoFiltrada AS long), 
        CAST(d.tratamentoFervida AS long), 
        CAST(d.tratamentoClorada AS long), 
        CAST(d.tratamentoMineral AS long), 
        CAST(d.tratamentoSemTratamento AS long), 
        CAST(d.tratamentoNaoInformado AS long), 
        CAST(d.tratamentoTotal AS long)) 
        FROM Determinantes d 
        WHERE UPPER(d.ubs) LIKE UPPER(CONCAT('%', :ubs, '%')) 
        ORDER BY d.ano DESC
        """
    )
    List<TratamentoDTO> findTratamentoByUbs(String ubs);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.TratamentoDTO(
            'AGREGADO - ' || d.bairro, 
        SUM(d.tratamentoFiltrada), 
        SUM(d.tratamentoFervida), 
        SUM(d.tratamentoClorada), 
        SUM(d.tratamentoMineral), 
        SUM(d.tratamentoSemTratamento), 
        SUM(d.tratamentoNaoInformado), 
        SUM(d.tratamentoTotal)) 
        FROM Determinantes d 
        WHERE UPPER(d.bairro) LIKE UPPER(CONCAT('%', :bairro, '%')) 
        GROUP BY d.ano, d.bairro 
        ORDER BY d.ano DESC
        """
    )
    List<TratamentoDTO> aggregateTratamentoByBairro(String bairro);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.EscoamentoDTO(
            d.ubs, 
        CAST(d.escoamentoRedeColetora AS long), 
        CAST(d.escoamentoFossaSeptica AS long), 
        CAST(d.escoamentoFossaRudimentar AS long), 
        CAST(d.escoamentoRioMar AS long), 
        CAST(d.escoamentoCeuAberto AS long), 
        CAST(d.escoamentoOutra AS long), 
        CAST(d.escoamentoNaoInformado AS long), 
        CAST(d.escoamentoTotal AS long)) 
        FROM Determinantes d 
        ORDER BY d.ano DESC, d.ubs ASC
        """
    )
    List<EscoamentoDTO> findAllEscoamento();

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.EscoamentoDTO(
            d.ubs, 
        CAST(d.escoamentoRedeColetora AS long), 
        CAST(d.escoamentoFossaSeptica AS long), 
        CAST(d.escoamentoFossaRudimentar AS long), 
        CAST(d.escoamentoRioMar AS long), 
        CAST(d.escoamentoCeuAberto AS long), 
        CAST(d.escoamentoOutra AS long), 
        CAST(d.escoamentoNaoInformado AS long), 
        CAST(d.escoamentoTotal AS long)) 
        FROM Determinantes d 
        WHERE UPPER(d.ubs) LIKE UPPER(CONCAT('%', :ubs, '%')) 
        ORDER BY d.ano DESC
        """
    )
    List<EscoamentoDTO> findEscoamentoByUbs(String ubs);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.EscoamentoDTO(
            'AGREGADO - ' || d.bairro, 
        SUM(d.escoamentoRedeColetora), 
        SUM(d.escoamentoFossaSeptica), 
        SUM(d.escoamentoFossaRudimentar), 
        SUM(d.escoamentoRioMar), 
        SUM(d.escoamentoCeuAberto), 
        SUM(d.escoamentoOutra), 
        SUM(d.escoamentoNaoInformado), 
        SUM(d.escoamentoTotal)) 
        FROM Determinantes d 
        WHERE UPPER(d.bairro) LIKE UPPER(CONCAT('%', :bairro, '%')) 
        GROUP BY d.ano, d.bairro 
        ORDER BY d.ano DESC
        """
    )
    List<EscoamentoDTO> aggregateEscoamentoByBairro(String bairro);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.LixoDTO(
            d.ubs, 
        CAST(d.lixoColetado AS long), 
        CAST(d.lixoQueimadoEnterrado AS long), 
        CAST(d.lixoCeuAberto AS long), 
        CAST(d.lixoOutro AS long), 
        CAST(d.lixoNaoInformado AS long), 
        CAST(d.lixoTotal AS long)) 
        FROM Determinantes d 
        ORDER BY d.ano DESC, d.ubs ASC
        """
    )
    List<LixoDTO> findAllLixo();

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.LixoDTO(
            d.ubs, 
        CAST(d.lixoColetado AS long), 
        CAST(d.lixoQueimadoEnterrado AS long), 
        CAST(d.lixoCeuAberto AS long), 
        CAST(d.lixoOutro AS long), 
        CAST(d.lixoNaoInformado AS long), 
        CAST(d.lixoTotal AS long)) 
        FROM Determinantes d 
        WHERE UPPER(d.ubs) LIKE UPPER(CONCAT('%', :ubs, '%')) 
        ORDER BY d.ano DESC
        """
    )
    List<LixoDTO> findLixoByUbs(String ubs);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.LixoDTO(
            'AGREGADO - ' || d.bairro, 
        SUM(d.lixoColetado), 
        SUM(d.lixoQueimadoEnterrado), 
        SUM(d.lixoCeuAberto), 
        SUM(d.lixoOutro), 
        SUM(d.lixoNaoInformado), 
        SUM(d.lixoTotal)) 
        FROM Determinantes d 
        WHERE UPPER(d.bairro) LIKE UPPER(CONCAT('%', :bairro, '%')) 
        GROUP BY d.ano, d.bairro 
        ORDER BY d.ano DESC
        """
    )
    List<LixoDTO> aggregateLixoByBairro(String bairro);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.RendaDTO(
            d.ubs, 
        CAST(d.rendaUmQuartoSalario AS long), 
        CAST(d.rendaMeioSalario AS long), 
        CAST(d.rendaUmSalario AS long), 
        CAST(d.rendaDoisSalarios AS long), 
        CAST(d.rendaTresSalarios AS long), 
        CAST(d.rendaQuatroSalarios AS long), 
        CAST(d.rendaAusencia AS long), 
        CAST(d.rendaAcimaQuatro AS long), 
        CAST(d.rendaNaoInformado AS long), 
        CAST(d.rendaTotal AS long)) 
        FROM Determinantes d 
        ORDER BY d.ano DESC, d.ubs ASC
        """
    )
    List<RendaDTO> findAllRenda();

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.RendaDTO(
            d.ubs, 
        CAST(d.rendaUmQuartoSalario AS long), 
        CAST(d.rendaMeioSalario AS long), 
        CAST(d.rendaUmSalario AS long), 
        CAST(d.rendaDoisSalarios AS long), 
        CAST(d.rendaTresSalarios AS long), 
        CAST(d.rendaQuatroSalarios AS long), 
        CAST(d.rendaAusencia AS long), 
        CAST(d.rendaAcimaQuatro AS long), 
        CAST(d.rendaNaoInformado AS long), 
        CAST(d.rendaTotal AS long)) 
        FROM Determinantes d 
        WHERE UPPER(d.ubs) LIKE UPPER(CONCAT('%', :ubs, '%')) 
        ORDER BY d.ano DESC
        """
    )
    List<RendaDTO> findRendaByUbs(String ubs);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.RendaDTO(
            'AGREGADO - ' || d.bairro, 
        SUM(d.rendaUmQuartoSalario), 
        SUM(d.rendaMeioSalario), 
        SUM(d.rendaUmSalario), 
        SUM(d.rendaDoisSalarios), 
        SUM(d.rendaTresSalarios), 
        SUM(d.rendaQuatroSalarios), 
        SUM(d.rendaAusencia), 
        SUM(d.rendaAcimaQuatro), 
        SUM(d.rendaNaoInformado), 
        SUM(d.rendaTotal)) 
        FROM Determinantes d 
        WHERE UPPER(d.bairro) LIKE UPPER(CONCAT('%', :bairro, '%')) 
        GROUP BY d.ano, d.bairro 
        ORDER BY d.ano DESC
        """
    )
    List<RendaDTO> aggregateRendaByBairro(String bairro);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.EducacaoDTO(
            d.ubs, 
        CAST(d.eduCreche AS long), 
        CAST(d.eduPreEscola AS long), 
        CAST(d.eduAlfabetizacao AS long), 
        CAST(d.edu1a4 AS long), 
        CAST(d.edu5a8 AS long), 
        CAST(d.eduFundamentalCompleto AS long), 
        CAST(d.eduMedio AS long), 
        CAST(d.eduSuperior AS long), 
        CAST(d.eduNaoInformado AS long), 
        CAST(d.eduTotal AS long)) 
        FROM Determinantes d 
        ORDER BY d.ano DESC, d.ubs ASC
        """
    )
    List<EducacaoDTO> findAllEducacao();

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.EducacaoDTO(
            d.ubs, 
        CAST(d.eduCreche AS long), 
        CAST(d.eduPreEscola AS long), 
        CAST(d.eduAlfabetizacao AS long), 
        CAST(d.edu1a4 AS long), 
        CAST(d.edu5a8 AS long), 
        CAST(d.eduFundamentalCompleto AS long), 
        CAST(d.eduMedio AS long), 
        CAST(d.eduSuperior AS long), 
        CAST(d.eduNaoInformado AS long), 
        CAST(d.eduTotal AS long)) 
        FROM Determinantes d 
        WHERE UPPER(d.ubs) LIKE UPPER(CONCAT('%', :ubs, '%')) 
        ORDER BY d.ano DESC
        """
    )
    List<EducacaoDTO> findEducacaoByUbs(String ubs);

    @Query("""
        SELECT new com.api.determinantes.sociais.Api.DTO.EducacaoDTO(
            'AGREGADO - ' || d.bairro, 
        SUM(d.eduCreche), 
        SUM(d.eduPreEscola), 
        SUM(d.eduAlfabetizacao), 
        SUM(d.edu1a4), 
        SUM(d.edu5a8), 
        SUM(d.eduFundamentalCompleto), 
        SUM(d.eduMedio), 
        SUM(d.eduSuperior), 
        SUM(d.eduNaoInformado), 
        SUM(d.eduTotal)) 
        FROM Determinantes d 
        WHERE UPPER(d.bairro) LIKE UPPER(CONCAT('%', :bairro, '%')) 
        GROUP BY d.ano, d.bairro 
        ORDER BY d.ano DESC
        """
    )
    List<EducacaoDTO> aggregateEducacaoByBairro(String bairro);
}