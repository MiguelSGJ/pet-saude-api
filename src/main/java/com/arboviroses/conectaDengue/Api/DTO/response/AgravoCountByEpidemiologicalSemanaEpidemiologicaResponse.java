package com.arboviroses.conectaDengue.Api.DTO.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse
{
    private List<AgravoCountBySemanaEpidemiologica> dengue;
    private List<AgravoCountBySemanaEpidemiologica> zika;
    private List<AgravoCountBySemanaEpidemiologica> chikungunya;

    public AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse(List<AgravoCountBySemanaEpidemiologica> list)
    {
        dengue = new ArrayList<>();
        zika = new ArrayList<>();
        chikungunya = new ArrayList<>();

        for (AgravoCountBySemanaEpidemiologica item : list) 
        {
            if (item.getAgravoId().equals("A90")) {
                dengue.add(item);
            } else if (item.getAgravoId().equals("A92.0")) {
                chikungunya.add(item);
            } else {
                zika.add(item);
            }
        }
    }

    public AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse(List<AgravoCountBySemanaEpidemiologica> list, boolean accumulated)
    {
        dengue = new ArrayList<>();
        zika = new ArrayList<>();
        chikungunya = new ArrayList<>();

        for (AgravoCountBySemanaEpidemiologica item : list) 
        {
            if (item.getAgravoId().equals("A90")) {
                if (dengue.size() > 0) {
                    AgravoCountBySemanaEpidemiologica beforeItem = dengue.get(dengue.size() - 1);
                    item.setCasesCount(beforeItem.getCasesCount() + item.getCasesCount());
                }
                
                dengue.add(item);
            } else if (item.getAgravoId().equals("A92.0")) {
                if (chikungunya.size() > 0) {
                    AgravoCountBySemanaEpidemiologica beforeItem = chikungunya.get(chikungunya.size() - 1);
                    item.setCasesCount(beforeItem.getCasesCount() + item.getCasesCount());
                }

                chikungunya.add(item);
            } else {
                if (zika.size() > 0) {
                    AgravoCountBySemanaEpidemiologica beforeItem = zika.get(zika.size() - 1);
                    item.setCasesCount(beforeItem.getCasesCount() + item.getCasesCount());
                }

                zika.add(item);
            }
        }
    }
}


