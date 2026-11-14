package com.arboviroses.conectaDengue.unit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arboviroses.conectaDengue.Api.DTO.request.NeighborhoodWeeklyPdfReportRequest;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Services.Bairros.BairroService;
import com.arboviroses.conectaDengue.Domain.Services.reports.NeighborhoodWeeklyPdfReportService;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.util.List;
import org.junit.jupiter.api.Test;

class NeighborhoodWeeklyPdfReportServiceTest {

    @Test
    void shouldApplyEveryDashboardFilterToNeighborhoodReport() throws Exception {
        BairroService bairroService = mock(BairroService.class);
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.buscarMaiorSemanaEpidemiologica(
            "A90", 2025, "CENTRO", "obitos", List.of("12")
        )).thenReturn(20);
        when(repository.buscarContagemSemanalPorBairro(
            "A90", 2025, "CENTRO", "obitos", 10, 12, List.of("12")
        )).thenReturn(List.of());

        NeighborhoodWeeklyPdfReportService service =
            new NeighborhoodWeeklyPdfReportService(bairroService, repository);
        byte[] pdf = service.generateReport(new NeighborhoodWeeklyPdfReportRequest(
            10, 12, 2025, "dengue_grave", "CENTRO", "obitos"
        ));

        verify(repository).buscarMaiorSemanaEpidemiologica(
            "A90", 2025, "CENTRO", "obitos", List.of("12")
        );
        verify(repository).buscarContagemSemanalPorBairro(
            "A90", 2025, "CENTRO", "obitos", 10, 12, List.of("12")
        );

        PdfReader reader = new PdfReader(pdf);
        String text = new PdfTextExtractor(reader).getTextFromPage(1);
        reader.close();

        assertThat(text)
            .contains("Semanas 10 a 12")
            .contains("Ano 2025")
            .contains("Agravo Dengue Grave")
            .contains("Bairro CENTRO")
            .contains("Escopo Obitos");
    }
}
