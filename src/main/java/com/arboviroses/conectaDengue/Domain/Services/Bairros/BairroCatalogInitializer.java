package com.arboviroses.conectaDengue.Domain.Services.Bairros;

import com.arboviroses.conectaDengue.Api.DTO.BairroSeedItem;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BairroCatalogInitializer implements CommandLineRunner {
    private final BairroService bairroService;

    @Override
    public void run(String... args) {
        bairroService.loadNeighborhoodCatalog(buildCatalog());
    }

    private List<BairroSeedItem> buildCatalog() {
        return List.of(
            new BairroSeedItem("ABOLICOES", "MOSSORO", List.of("ABOLICAO I", "ABOLICAO II", "ABOLICAO III", "ABOLICAO IV", "ABOLICAO V", "COMUNIDADE DO CIGANO", "TRES VINTENS, BOA ESPERANCA E COMUNIDADE SEM TERRA", "POUSADA DOS TERMAS")),
            new BairroSeedItem("AEROPORTO", "MOSSORO", List.of("COMUNIDADE MACARRAO", "VILA DO IPASA", "QUIXABEIRINHA")),
            new BairroSeedItem("ALTO DE SAO MANOEL", "MOSSORO", List.of("WALFREDO GURGEL", "URICK GRAFF", "COAB")),
            new BairroSeedItem("ALTO DO SUMARE", "MOSSORO", List.of("CIDADE JARDIM", "MONTE OLIMPO")),
            new BairroSeedItem("ALTO DA CONCEICAO", "MOSSORO", List.of("PEREIROS", "COMUNIDADE DO PANTANAL")),
            new BairroSeedItem("ALTO DA BELA VISTA", "MOSSORO", List.of("CONJ MARICIO MARINHO", "QUINTAS - ALPHAVILLE", "SANVILLE")),
            new BairroSeedItem("ALAGADOS", "MOSSORO", List.of()),
            new BairroSeedItem("BARROCAS", "MOSSORO", List.of("CONJ. DE FREITAS NOBRE")),
            new BairroSeedItem("BOM JARDIM", "MOSSORO", List.of()),
            new BairroSeedItem("BELO HORIZONTE", "MOSSORO", List.of("CARNAUBAL")),
            new BairroSeedItem("BOA VISTA", "MOSSORO", List.of()),
            new BairroSeedItem("BOM JESUS", "MOSSORO", List.of()),
            new BairroSeedItem("CENTRO", "MOSSORO", List.of()),
            new BairroSeedItem("COSTA E SILVA", "MOSSORO", List.of("TEIMOSOS", "CONJ. GERALDO MELO")),
            new BairroSeedItem("DOM JAIME CAMARA", "MOSSORO", List.of("MALVINAS", "CONJ. NOVA VIDA", "TRAQUILIM", "JARDIM DA PALMEIRAS")),
            new BairroSeedItem("DOZE ANOS", "MOSSORO", List.of()),
            new BairroSeedItem("GOV DIX SEPT ROSADO", "MOSSORO", List.of("FORNO VELHO", "BOM PASTOR", "VERONIQUE", "BOULEVARD")),
            new BairroSeedItem("ITAPETINGA", "MOSSORO", List.of("CIDADE OESTE")),
            new BairroSeedItem("ILHA DE SANTA LUZIA", "MOSSORO", List.of()),
            new BairroSeedItem("LAGOA DO MATO", "MOSSORO", List.of("ALTO DO XEREM")),
            new BairroSeedItem("MONS ALFREDO SIMONNETI", "MOSSORO", List.of("MONS. AMERICO", "OURO NEGRO", "PORTAL DO SOL")),
            new BairroSeedItem("NOVA BETANIA", "MOSSORO", List.of()),
            new BairroSeedItem("PINTOS", "MOSSORO", List.of()),
            new BairroSeedItem("PAREDOES", "MOSSORO", List.of("SAO JOSE")),
            new BairroSeedItem("PLANALTO 13 DE MAIO", "MOSSORO", List.of("ALAMEDA DOS CAJUEIROS", "LIBERDADE I","LIBERDADE II", "PAPOCO", "INOCOOP")),
            new BairroSeedItem("REDENCAO", "MOSSORO", List.of("CONJ. INTEGRACAO", "CONJ. INDEPENCIA I E II", "LOTEAMENTO JARDINS")),
            new BairroSeedItem("RINCAO", "MOSSORO", List.of("CONJ. VINGT ROSADO", "ALTO DA PELONHA", "ODETE ROSADO", "ALTO DAS BRISAS", "PARQUE UNIVERSITARIO")),
            new BairroSeedItem("SANTO ANTONIO", "MOSSORO", List.of("SANTA HELENA", "WILSON ROSADO", "ESTRADA DA RAIZ", "CONJ. SANDRA ROSADO", "CONJ JOSE AGRIPINO")),
            new BairroSeedItem("SANTA DELMIRA", "MOSSORO", List.of("PARQUE DAS ROSAS", "CONJ NOVA ESPERANCA", "CONJ. ROSILANDIA", "BOA ESPERANCA", "CONJ. RESISTENCIA", "PROMORAR")),
            new BairroSeedItem("SANTA JULIA", "MOSSORO", List.of("NOVA MOSSORO", "ROYALVILLE"))
        );
    }
}
