package com.arboviroses.conectaDengue.Utils;

import com.arboviroses.conectaDengue.Api.Exceptions.InvalidAgravoException;

public class ConvertNameToIdAgravo {
    public static String convert(String agravo) throws InvalidAgravoException {
        switch(agravo.toUpperCase()) {
            case "CHIKUNGUNYA":
                agravo = "A92.0";
                break;
            case "DENGUE":
                agravo = "A90";
                break;
            case "ZIKA":
                agravo = "A928";
                break;
            default: 
                throw new InvalidAgravoException("Valor inválido: " + agravo + ". valores aceitos: zika, dengue, chikungunya");
        }

        return agravo;
    }
}
