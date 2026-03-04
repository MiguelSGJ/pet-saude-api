package com.arboviroses.conectaDengue.Utils; 
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;

public class ConvertCSVLineToNotifications {
    public static Notification convertCsvLineToNotificationObject(String[] line, List<String> header)
    {   
        Date dataNascimento = StringToDateCSV.ConvertStringToDate(line[header.indexOf("DT_NASC")]);
        Date dataNotification = StringToDateCSV.ConvertStringToDate(line[header.indexOf("DT_NOTIFIC")]);
        int epidemiologicalWeek = calculateEpidemiologicalWeek(dataNotification);
        int idade = extractIdade(header, line);

        if (idade == 0) {
            idade = getYearsDifference(dataNascimento, dataNotification);
        }

        return new Notification(
            Long.valueOf(line[header.indexOf("NU_NOTIFIC")]),
            line[header.indexOf("ID_AGRAVO")],
            idade,
            dataNotification,
            dataNascimento,
            line[header.indexOf("CLASSI_FIN")],
            line[header.indexOf("CS_SEXO")],
            line[header.indexOf("ID_BAIRRO")] != "" ? Integer.valueOf(line[header.indexOf("ID_BAIRRO")]) : 0,
            line[header.indexOf("NM_BAIRRO")],
            line[header.indexOf("EVOLUCAO")],
            epidemiologicalWeek
         );
    }

    public static int calculateEpidemiologicalWeek(Date date) {
        LocalDate caseDate = date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();;
        
        LocalDate firstSundayOfYear = caseDate.withDayOfYear(1);
        while (firstSundayOfYear.getDayOfWeek() != DayOfWeek.SUNDAY) {
            firstSundayOfYear = firstSundayOfYear.plusDays(1);
        }
        
        int daysSinceFirstSunday = (int) firstSundayOfYear.until(caseDate, java.time.temporal.ChronoUnit.DAYS);
        int epidemiologicalWeek = (daysSinceFirstSunday / 7) + 1;
        
        return epidemiologicalWeek;
    }

    private static Integer extractIdade(List<String> header, String[] line) {
        int idade = 0;
        int idadeIndex = header.indexOf("IDADE");

        if (idadeIndex != -1 && !line[idadeIndex].isEmpty()) {            
            if (line[idadeIndex].contains(".")) {
                String[] splitIdade = line[idadeIndex].split("\\.");
                idade = Integer.valueOf(splitIdade[0]);
            } else {
                idade = Integer.valueOf(line[idadeIndex]);
            }
        }
        
        return idade;
    }

    private static int getYearsDifference(Date date1, Date date2) { 
        if (date1 == null || date2 == null) {
            return 0;
        }

        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period period = Period.between(localDate1, localDate2);
        return period.getYears();
    }
}
