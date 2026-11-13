package com.arboviroses.conectaDengue.unit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arboviroses.conectaDengue.Api.DTO.response.NotificationErrorWithCategoryDTO;
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationsErrorService;
import com.arboviroses.conectaDengue.Domain.Services.reports.ErrorsPdfReportService;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorsPdfReportServiceTest {

    @Test
    void shouldGeneratePdfWithTheRequestedFiltersAndOnlyFilteredRecords() throws Exception {
        NotificationsErrorService notificationsErrorService = mock(NotificationsErrorService.class);
        long startDate = 1_700_000_000_000L;
        long endDate = 1_700_086_399_999L;
        NotificationErrorWithCategoryDTO record = new NotificationErrorWithCategoryDTO();
        record.setIdAgravo("A90");
        record.setDataPrimeiroSintoma(new Date(startDate));
        record.setNomeBairro("CENTRO");
        record.setSexo("F");
        record.setClassificacao("10");
        record.setEvolucao("1");
        record.setSemanaEpidemiologica(46);
        record.setCategory("SEXO_NAO_INFORMADO");
        when(notificationsErrorService.getAllErrorsFiltered(
            "SEXO_NAO_INFORMADO", startDate, endDate, "A90"
        )).thenReturn(List.of(record));

        ErrorsPdfReportService service = new ErrorsPdfReportService(notificationsErrorService);
        byte[] pdf = service.generateReport("SEXO_NAO_INFORMADO", startDate, endDate, "A90");

        verify(notificationsErrorService).getAllErrorsFiltered(
            "SEXO_NAO_INFORMADO", startDate, endDate, "A90"
        );

        PdfReader reader = new PdfReader(pdf);
        String text = new PdfTextExtractor(reader).getTextFromPage(1);
        reader.close();

        assertThat(text)
            .contains("Problema: Sexo não informado")
            .contains("Doença: Dengue")
            .contains("Total: 1 registro(s)")
            .contains("CENTRO");
    }
}
