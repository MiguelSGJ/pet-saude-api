package com.arboviroses.conectaDengue.Domain.Entities.Lira;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lira")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bairro;

    @Column(name = "total_imoveis_inspecionados")
    private Integer totalImoveisInsp;

    @Column(name = "total_imoveis_positivos")
    private Integer totalImoveisPos;

    @Column(name = "indice_infestacao_predial")
    private Double indiceInfestacaoPredial;

    @Column(name = "total_depositos_positivos")
    private Integer totalDepositosPos;

    private double indiceBreteau;
    private int ano;
    private Integer liraNumber;

    @Column(name = "deposito_a1")
    private Integer depositoA1;

    @Column(name = "deposito_a2")
    private Integer depositoA2;

    @Column(name = "deposito_b")
    private Integer depositoB;

    @Column(name = "deposito_c")
    private Integer depositoC;

    @Column(name = "deposito_d1")
    private Integer depositoD1;

    @Column(name = "deposito_d2")
    private Integer depositoD2;

    @Column(name = "deposito_e")
    private Integer depositoE;
}
