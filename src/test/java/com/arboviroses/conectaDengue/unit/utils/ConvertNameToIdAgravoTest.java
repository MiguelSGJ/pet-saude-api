package com.arboviroses.conectaDengue.unit.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arboviroses.conectaDengue.Api.Exceptions.InvalidAgravoException;
import com.arboviroses.conectaDengue.Utils.ConvertNameToIdAgravo;

public class ConvertNameToIdAgravoTest {
    @Test
    public void test_if_convert_name_to_idAgravo_return_correct_names() throws InvalidAgravoException
    {
        String dengue = ConvertNameToIdAgravo.convert("dengue"); 
        String zika = ConvertNameToIdAgravo.convert("zika");
        String chikungunya = ConvertNameToIdAgravo.convert("chikungunya");

        Assertions.assertEquals(dengue, "A90");
        Assertions.assertEquals(zika, "A928");
        Assertions.assertEquals(chikungunya, "A92.0");
    }

    @Test
    public void test_if_convert_name_to_idAgravo_trows_invalidIdAgravoException()
    {
        String invalidAgravo = "Test value";

        String errorMessage = "Valor inválido: " + invalidAgravo + ". valores aceitos: zika, dengue, chikungunya";

        String message = Assertions.assertThrows(InvalidAgravoException.class, () -> {
            ConvertNameToIdAgravo.convert(invalidAgravo);
        }).getMessage();

        Assertions.assertEquals(errorMessage, message);
    }
}
