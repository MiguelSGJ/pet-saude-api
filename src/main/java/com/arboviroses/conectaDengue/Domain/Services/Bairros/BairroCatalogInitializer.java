package com.arboviroses.conectaDengue.Domain.Services.Bairros;

import com.arboviroses.conectaDengue.Api.DTO.BairroSeedItem;
import com.arboviroses.conectaDengue.Api.DTO.BairroCoordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

@Component
@RequiredArgsConstructor
public class BairroCatalogInitializer implements CommandLineRunner {
    private final BairroService bairroService;

    @Override
    public void run(String... args) {
        bairroService.loadNeighborhoodCatalog(buildCatalog());
        bairroService.loadWeatherCoordinates(buildWeatherCoordinates());
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

    private Map<String, BairroCoordinates> buildWeatherCoordinates() {
        return Map.ofEntries(
            entry("ABOLICOES", new BairroCoordinates(-5.1580539, -37.3606673)),
            entry("AEROPORTO", new BairroCoordinates(-5.1970112, -37.3695298)),
            entry("ALTO DE SAO MANOEL", new BairroCoordinates(-5.2101249, -37.3334201)),
            entry("ALTO DO SUMARE", new BairroCoordinates(-5.2331156, -37.3370185)),
            entry("ALTO DA CONCEICAO", new BairroCoordinates(-5.2004880, -37.3479802)),
            entry("ALTO DA BELA VISTA", new BairroCoordinates(-5.2125869, -37.3432887)),
            entry("ALAGADOS", new BairroCoordinates(-5.2153718, -37.3516995)),
            entry("BARROCAS", new BairroCoordinates(-5.1655503, -37.3322547)),
            entry("BOM JARDIM", new BairroCoordinates(-5.1791963, -37.3381264)),
            entry("BELO HORIZONTE", new BairroCoordinates(-5.2106005, -37.3574203)),
            entry("BOA VISTA", new BairroCoordinates(-5.1947398, -37.3535583)),
            entry("BOM JESUS", new BairroCoordinates(-5.2294878, -37.3614192)),
            entry("CENTRO", new BairroCoordinates(-5.1920530, -37.3413511)),
            entry("COSTA E SILVA", new BairroCoordinates(-5.2103888, -37.3143152)),
            entry("DOM JAIME CAMARA", new BairroCoordinates(-5.2284588, -37.3146408)),
            entry("DOZE ANOS", new BairroCoordinates(-5.1897814, -37.3507153)),
            entry("GOV DIX SEPT ROSADO", new BairroCoordinates(-5.2014882, -37.3649222)),
            entry("ITAPETINGA", new BairroCoordinates(-5.2122711, -37.3864290)),
            entry("ILHA DE SANTA LUZIA", new BairroCoordinates(-5.2001079, -37.3391123)),
            entry("LAGOA DO MATO", new BairroCoordinates(-5.2051842, -37.3600530)),
            entry("MONS ALFREDO SIMONNETI", new BairroCoordinates(-5.1844976, -37.3691797)),
            entry("NOVA BETANIA", new BairroCoordinates(-5.1787680, -37.3666830)),
            entry("PINTOS", new BairroCoordinates(-5.1797404, -37.3176846)),
            entry("PAREDOES", new BairroCoordinates(-5.1800072, -37.3305597)),
            entry("PLANALTO 13 DE MAIO", new BairroCoordinates(-5.2199344, -37.3384363)),
            entry("REDENCAO", new BairroCoordinates(-5.1423830, -37.3463139)),
            entry("RINCAO", new BairroCoordinates(-5.1970980, -37.2880033)),
            entry("SANTO ANTONIO", new BairroCoordinates(-5.1663384, -37.3432675)),
            entry("SANTA DELMIRA", new BairroCoordinates(-5.1523571, -37.3562337)),
            entry("SANTA JULIA", new BairroCoordinates(-5.1353757, -37.3281929))
        );
    }
}
