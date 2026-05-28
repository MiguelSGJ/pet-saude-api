package com.arboviroses.conectaDengue.Utils;

import com.arboviroses.conectaDengue.Api.Exceptions.InvalidAgravoException;

public class ConvertNameToIdAgravo {
    public static String convert(String agravo) throws InvalidAgravoException {
        switch(agravo.toUpperCase()) {
            case "CHIKUNGUNYA":
                agravo = "A92.0";
                break;
            case "DENGUE":
            case "DENGUE_GERAL":
            case "DENGUE_CLASSICA":
            case "DENGUE_ALARMANTE":
            case "DENGUE_GRAVE":
                agravo = "A90";
                break;
            case "ZIKA":
                agravo = "A928";
                break;
            default:
                throw new InvalidAgravoException("Valor inválido: " + agravo + ". Valores aceitos: zika, dengue, dengue_geral, dengue_classica, dengue_alarmante, dengue_grave, chikungunya");
        }

        return agravo;
    }
}
