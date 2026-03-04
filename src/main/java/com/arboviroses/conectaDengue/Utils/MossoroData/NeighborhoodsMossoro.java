package com.arboviroses.conectaDengue.Utils.MossoroData;

import java.util.HashSet;
import java.util.Set;
import com.arboviroses.conectaDengue.Utils.Search.SearchAlgorithms;

public class NeighborhoodsMossoro
{
    private static final Set<String> neighborhoods;

    static {
        neighborhoods = new HashSet<>();
        neighborhoods.add("Abolição");
        neighborhoods.add("AEROPORTO");
        neighborhoods.add("ALTO DA CONCEIÇÃO");
        neighborhoods.add("ALTO DE SÃO MANOEL");
        neighborhoods.add("ALTO DO SUMARÉ");
        neighborhoods.add("ÁREA RURAL DE MOSSORÓ");
        neighborhoods.add("BARROCAS");
        neighborhoods.add("BELA VISTA");
        neighborhoods.add("BELO HORIZONTE");
        neighborhoods.add("BOA VISTA");
        neighborhoods.add("BOM JARDIM");
        neighborhoods.add("BOM JESUS");
        neighborhoods.add("CENTRO");
        neighborhoods.add("DIX-SEPT ROSADO");
        neighborhoods.add("DOM JAIME CÂMARA");
        neighborhoods.add("DOZE ANOS");
        neighborhoods.add("ILHA DE SANTA LUZIA");
        neighborhoods.add("ITAPETINGA");
        neighborhoods.add("LAGOA DO MATO");
        neighborhoods.add("MONSENHOR AMÉRICO");
        neighborhoods.add("NOVA BETÂNIA");
        neighborhoods.add("PAREDÕES");
        neighborhoods.add("PINTOS");
        neighborhoods.add("PLANALTO TREZE DE MAIO");
        neighborhoods.add("PRESIDENTE COSTA E SILVA");
        neighborhoods.add("REDENÇÃO");
        neighborhoods.add("RINCÃO");
        neighborhoods.add("SANTA DELMIRA");
        neighborhoods.add("SANTA JÚLIA");
        neighborhoods.add("SANTO ANTÔNIO");
    }

    public static boolean contains(String key)
    {
        return neighborhoods.contains(key);
    }

    public static String search(String key) 
    {
        int calculatedDistance, shortestDistance = 3;
        String correctedNeighborhood = null;

        if (contains(key)) {
            return key;
        }

        for (String neighborhood : neighborhoods) {
            if ((calculatedDistance = SearchAlgorithms.levenstein(key, neighborhood)) <= shortestDistance) {
                shortestDistance = calculatedDistance;
                correctedNeighborhood = neighborhood;
            }
        }

        return correctedNeighborhood;
    }

    public static Set<String> getNeighborhoods()
    {
        return new HashSet<>(neighborhoods);
    }
}