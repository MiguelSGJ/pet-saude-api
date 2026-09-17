package com.arboviroses.conectaDengue.Domain.Entities.Lira;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "lira_parametros",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_lira_parametros_ano_ciclo",
                columnNames = {"ano", "lira_number"}
        )
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiraParametros {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ano;

    @Column(name = "lira_number", nullable = false)
    private Integer liraNumber;

    @Column(name = "limite_alerta", nullable = false)
    private Double limiteAlerta;

    @Column(name = "limite_risco", nullable = false)
    private Double limiteRisco;
}
