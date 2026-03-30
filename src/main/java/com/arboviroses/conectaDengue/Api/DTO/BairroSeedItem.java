package com.arboviroses.conectaDengue.Api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BairroSeedItem {
    private String bairro;
    private String cidade;
    private List<String> subBairros;
}
