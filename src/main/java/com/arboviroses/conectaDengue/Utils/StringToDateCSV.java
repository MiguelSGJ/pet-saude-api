package com.arboviroses.conectaDengue.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringToDateCSV
{
    public static Date ConvertStringToDate(String dateString) 
    {
        if (dateString == null || dateString == "") {
            return null;
        }

        SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String[] dateInfos = dateString.split("/");
        
        if (dateInfos.length < 3) {
            return null;
        }   

        if (dateInfos[0].length() > 2 || dateInfos[1].length() > 2 || dateInfos[2].length() > 4) {
            return null;        
        }

        String mes = formatFieldsOfDate(dateInfos[0], "0", 2);
        String dia = formatFieldsOfDate(dateInfos[1], "0", 2);
        String formatedDateComplete = dia + "/" + mes + "/" + dateInfos[2];

        try {
            Date finalDate = defaultDateFormat.parse(formatedDateComplete);
            return finalDate;
        } catch (ParseException e) {
            return null;
        }
    }

    private static String formatFieldsOfDate(String field, String stringToAdd, int lenght)
    {   
        while (field.length() < lenght) {
            field = stringToAdd + field;
        }

        return field;
    }
}