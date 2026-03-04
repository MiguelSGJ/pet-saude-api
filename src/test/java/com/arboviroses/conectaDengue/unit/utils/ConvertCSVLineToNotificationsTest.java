package com.arboviroses.conectaDengue.unit.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;

import java.util.List;
import java.text.ParseException;
import java.util.Arrays;

import static com.arboviroses.conectaDengue.Utils.ConvertCSVLineToNotifications.convertCsvLineToNotificationObject;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConvertCSVLineToNotificationsTest {
    @Test
    public void test_if_method_convert_returns_an_notification_object() throws NumberFormatException, ParseException
    {
        // arrage
        String[] line = {"500", "A90", "10/10/2006", "10/10/2006", "100", "M", "ABOLICAO", "1", "27", "42"};
        List<String> header = Arrays.asList(
            "NU_NOTIFIC",
            "ID_AGRAVO",
            "DT_NOTIFIC",
            "DT_NASC",
            "CLASSI_FIN",
            "CS_SEXO",
            "NM_BAIRRO",
            "EVOLUCAO",
            "IDADE", 
            "ID_BAIRRO"
        );

        // act
        Notification notification = convertCsvLineToNotificationObject(line, header);

        // assert
        Assertions.assertThat(notification).isNotNull();
        assertEquals(500, notification.getIdNotification());
        assertEquals("A90", notification.getIdAgravo());
        assertEquals("Tue Oct 10 00:00:00 BRT 2006", notification.getDataNascimento().toString());
        assertEquals("Tue Oct 10 00:00:00 BRT 2006", notification.getDataNotification().toString());
        assertEquals("100", notification.getClassificacao());
        assertEquals("M", notification.getSexo());
        assertEquals("ABOLICAO", notification.getNomeBairro());
        assertEquals("1", notification.getEvolucao());
    }
}
