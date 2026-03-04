package com.arboviroses.conectaDengue.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "social_determinants")
public class Determinantes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ano;
    private String ubs;
    private String bairro;

    // Abastecimento de Água
    private Integer aguaRede;
    private Integer aguaPoco;
    private Integer aguaCisterna;
    private Integer aguaCarroPipe;
    private Integer aguaOutro;
    private Integer aguaNaoInformado;
    private Integer aguaTotal;

    // Tratamento de água/armazenamento
    private Integer tratamentoFiltrada;
    private Integer tratamentoFervida;
    private Integer tratamentoClorada;
    private Integer tratamentoMineral;
    private Integer tratamentoSemTratamento;
    private Integer tratamentoNaoInformado;
    private Integer tratamentoTotal;

    // Forma de escoamento do banheiro ou sanitário
    private Integer escoamentoRedeColetora;
    private Integer escoamentoFossaSeptica;
    private Integer escoamentoFossaRudimentar;
    private Integer escoamentoRioMar;
    private Integer escoamentoCeuAberto;
    private Integer escoamentoOutra;
    private Integer escoamentoNaoInformado;
    private Integer escoamentoTotal;

    // Destino do Lixo
    private Integer lixoColetado;
    private Integer lixoQueimadoEnterrado;
    private Integer lixoCeuAberto;
    private Integer lixoOutro;
    private Integer lixoNaoInformado;
    private Integer lixoTotal;

    // Renda Familiar
    private Integer rendaUmQuartoSalario;
    private Integer rendaMeioSalario;
    private Integer rendaUmSalario;
    private Integer rendaDoisSalarios;
    private Integer rendaTresSalarios;
    private Integer rendaQuatroSalarios;
    private Integer rendaAusencia;
    private Integer rendaAcimaQuatro;
    private Integer rendaNaoInformado;
    private Integer rendaTotal;

    // Perfil educacional
    private Integer eduCreche;
    private Integer eduPreEscola;
    private Integer eduAlfabetizacao;
    private Integer edu1a4;
    private Integer edu5a8;
    private Integer eduFundamentalCompleto;
    private Integer eduFundamentalEspecial;
    private Integer eduEja1a4;
    private Integer eduEja5a8;
    private Integer eduMedio;
    private Integer eduMedioEspecial;
    private Integer eduMedioEja;
    private Integer eduSuperior;
    private Integer eduMobral;
    private Integer eduNenhum;
    private Integer eduNaoInformado;
    private Integer eduTotal;
}
