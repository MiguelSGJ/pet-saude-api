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
    private long dengueTotal;
    private long zikaTotal;
    private long chikungunyaTotal;

    public AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse(List<AgravoCountBySemanaEpidemiologica> list, Integer semanaInicial, Integer semanaFinal)
    {
        dengue = new ArrayList<>();
        zika = new ArrayList<>();
        chikungunya = new ArrayList<>();
        int start = semanaInicial != null ? semanaInicial : 1;

        for (AgravoCountBySemanaEpidemiologica item : list)
        {
            if (item.getAgravoId().equals("A90")) {
                fillGap(dengue, item.getEpidemiologicalWeek(), "A90", 0L, start);
                dengue.add(item);
            } else if (item.getAgravoId().equals("A92.0")) {
                fillGap(chikungunya, item.getEpidemiologicalWeek(), "A92.0", 0L, start);
                chikungunya.add(item);
            } else {
                fillGap(zika, item.getEpidemiologicalWeek(), item.getAgravoId(), 0L, start);
                zika.add(item);
            }
        }

        if (semanaFinal != null) {
            fillTail(dengue, "A90", semanaFinal, start, 0L);
            fillTail(chikungunya, "A92.0", semanaFinal, start, 0L);
            fillTail(zika, "B06", semanaFinal, start, 0L);
        }

        dengueTotal = dengue.stream().mapToLong(AgravoCountBySemanaEpidemiologica::getCasesCount).sum();
        zikaTotal = zika.stream().mapToLong(AgravoCountBySemanaEpidemiologica::getCasesCount).sum();
        chikungunyaTotal = chikungunya.stream().mapToLong(AgravoCountBySemanaEpidemiologica::getCasesCount).sum();
    }

    public AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse(List<AgravoCountBySemanaEpidemiologica> list, boolean accumulated, Integer semanaInicial, Integer semanaFinal)
    {
        dengue = new ArrayList<>();
        zika = new ArrayList<>();
        chikungunya = new ArrayList<>();
        int start = semanaInicial != null ? semanaInicial : 1;

        for (AgravoCountBySemanaEpidemiologica item : list)
        {
            if (item.getAgravoId().equals("A90")) {
                long prev = dengue.isEmpty() ? 0L : dengue.get(dengue.size() - 1).getCasesCount();
                fillGap(dengue, item.getEpidemiologicalWeek(), "A90", prev, start);
                item.setCasesCount(prev + item.getCasesCount());
                dengue.add(item);
            } else if (item.getAgravoId().equals("A92.0")) {
                long prev = chikungunya.isEmpty() ? 0L : chikungunya.get(chikungunya.size() - 1).getCasesCount();
                fillGap(chikungunya, item.getEpidemiologicalWeek(), "A92.0", prev, start);
                item.setCasesCount(prev + item.getCasesCount());
                chikungunya.add(item);
            } else {
                long prev = zika.isEmpty() ? 0L : zika.get(zika.size() - 1).getCasesCount();
                fillGap(zika, item.getEpidemiologicalWeek(), item.getAgravoId(), prev, start);
                item.setCasesCount(prev + item.getCasesCount());
                zika.add(item);
            }
        }

        if (semanaFinal != null) {
            long lastDengue = dengue.isEmpty() ? 0L : dengue.get(dengue.size() - 1).getCasesCount();
            fillTail(dengue, "A90", semanaFinal, start, lastDengue);
            long lastChik = chikungunya.isEmpty() ? 0L : chikungunya.get(chikungunya.size() - 1).getCasesCount();
            fillTail(chikungunya, "A92.0", semanaFinal, start, lastChik);
            long lastZika = zika.isEmpty() ? 0L : zika.get(zika.size() - 1).getCasesCount();
            fillTail(zika, "B06", semanaFinal, start, lastZika);
        }

        dengueTotal = dengue.isEmpty() ? 0L : dengue.get(dengue.size() - 1).getCasesCount();
        zikaTotal = zika.isEmpty() ? 0L : zika.get(zika.size() - 1).getCasesCount();
        chikungunyaTotal = chikungunya.isEmpty() ? 0L : chikungunya.get(chikungunya.size() - 1).getCasesCount();
    }

    private void fillGap(List<AgravoCountBySemanaEpidemiologica> list, int targetWeek, String agravoId, long fillCount, int startWeek) {
        int lastWeek = list.isEmpty() ? startWeek - 1 : list.get(list.size() - 1).getEpidemiologicalWeek();
        for (int week = lastWeek + 1; week < targetWeek; week++) {
            AgravoCountBySemanaEpidemiologica zero = new AgravoCountBySemanaEpidemiologica();
            zero.setEpidemiologicalWeek(week);
            zero.setCasesCount(fillCount);
            zero.setAgravoId(agravoId);
            list.add(zero);
        }
    }

    private void fillTail(List<AgravoCountBySemanaEpidemiologica> list, String agravoId, int semanaFinal, int startWeek, long fillCount) {
        int lastWeek = list.isEmpty() ? startWeek - 1 : list.get(list.size() - 1).getEpidemiologicalWeek();
        for (int week = lastWeek + 1; week <= semanaFinal; week++) {
            AgravoCountBySemanaEpidemiologica zero = new AgravoCountBySemanaEpidemiologica();
            zero.setEpidemiologicalWeek(week);
            zero.setCasesCount(fillCount);
            zero.setAgravoId(agravoId);
            list.add(zero);
        }
    }
}
