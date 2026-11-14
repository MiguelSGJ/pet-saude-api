package com.arboviroses.conectaDengue.unit.services;

import com.arboviroses.conectaDengue.Api.DTO.response.NotificationErrorWithCategoryDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.NotificationWithError;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationWithErrorRepository;
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationsErrorService;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationsErrorServiceTest {

    @Test
    void shouldFilterInconsistenciesByFirstSymptomsDate() {
        NotificationWithErrorRepository repository = mock(NotificationWithErrorRepository.class);
        NotificationWithError before = completeError(new Date(2_000L), new Date(1_000L));
        NotificationWithError after = completeError(new Date(1_000L), new Date(3_000L));
        when(repository.findAllWithMaxIteration()).thenReturn(List.of(before, after));

        NotificationsErrorService service = new NotificationsErrorService(repository);
        List<NotificationErrorWithCategoryDTO> result =
            service.getAllErrorsFiltered(null, 2_000L, null, null);

        assertThat(result)
            .extracting(NotificationErrorWithCategoryDTO::getDataPrimeiroSintoma)
            .containsExactly(new Date(3_000L));
    }

    @Test
    void shouldFilterInconsistenciesByProblemCategory() {
        NotificationWithErrorRepository repository = mock(NotificationWithErrorRepository.class);
        NotificationWithError missingSex = completeError(new Date(1_000L), new Date(1_000L));
        missingSex.setSexo(null);
        NotificationWithError missingEvolution = completeError(new Date(2_000L), new Date(2_000L));
        missingEvolution.setEvolucao(null);
        when(repository.findAllWithMaxIteration()).thenReturn(List.of(missingSex, missingEvolution));

        NotificationsErrorService service = new NotificationsErrorService(repository);

        assertThat(service.getAllErrorsFiltered("SEXO_NAO_INFORMADO", null, null, null))
            .singleElement()
            .extracting(NotificationErrorWithCategoryDTO::getCategory)
            .isEqualTo("SEXO_NAO_INFORMADO");
    }

    @Test
    void shouldUseTheSameCombinedFiltersForTableAndPdf() {
        NotificationWithErrorRepository repository = mock(NotificationWithErrorRepository.class);
        NotificationWithError matching = completeError(new Date(500L), new Date(2_500L));
        matching.setSexo(null);
        NotificationWithError wrongDisease = completeError(new Date(500L), new Date(2_500L));
        wrongDisease.setSexo(null);
        wrongDisease.setIdAgravo("A928");
        NotificationWithError outsidePeriod = completeError(new Date(500L), new Date(4_000L));
        outsidePeriod.setSexo(null);
        when(repository.findAllWithMaxIteration()).thenReturn(List.of(matching, wrongDisease, outsidePeriod));

        NotificationsErrorService service = new NotificationsErrorService(repository);
        List<NotificationErrorWithCategoryDTO> pdfRecords = service.getAllErrorsFiltered(
            "SEXO_NAO_INFORMADO", 2_000L, 3_000L, "A90"
        );
        Page<NotificationErrorWithCategoryDTO> tableRecords = service.getAllErrorsPaginated(
            PageRequest.of(0, 20), "SEXO_NAO_INFORMADO", 2_000L, 3_000L, "A90"
        );

        assertThat(pdfRecords).extracting(NotificationErrorWithCategoryDTO::getIdNotification)
            .containsExactly(matching.getIdNotification());
        assertThat(tableRecords.getContent())
            .extracting(NotificationErrorWithCategoryDTO::getIdNotification)
            .containsExactly(matching.getIdNotification());
    }

    private NotificationWithError completeError(Date notificationDate, Date symptomsDate) {
        return NotificationWithError.builder()
            .idNotification(symptomsDate.getTime())
            .idAgravo("A90")
            .idadePaciente(20)
            .dataNotification(notificationDate)
            .dataPrimeiroSintoma(symptomsDate)
            .classificacao("10")
            .sexo("F")
            .idBairro(1)
            .nomeBairro("CENTRO")
            .evolucao("1")
            .semanaEpidemiologica(1)
            .iteration(1)
            .build();
    }
}
