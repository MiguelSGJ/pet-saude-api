package com.arboviroses.conectaDengue.Domain.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "bairros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bairro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cidade;

    private Double latitude;

    private Double longitude;

    private String coordinatesSource;

    private OffsetDateTime coordinatesUpdatedAt;

    @OneToMany(mappedBy = "bairro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubBairro> subBairros = new ArrayList<>();

    public void addSubBairro(SubBairro subBairro) {
        subBairro.setBairro(this);
        this.subBairros.add(subBairro);
    }
}
