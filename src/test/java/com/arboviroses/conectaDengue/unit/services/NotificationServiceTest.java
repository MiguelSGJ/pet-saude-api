package com.arboviroses.conectaDengue.unit.services;

import com.arboviroses.conectaDengue.Api.DTO.request.NotificationBatchDTO;
import com.arboviroses.conectaDengue.Api.DTO.request.NotificationDataDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Bairro;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import com.arboviroses.conectaDengue.Domain.Services.Bairros.BairroService;
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationService;
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationsErrorService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Test
    void shouldIncludeInconsistentRecordsInDashboardTotal() throws Exception {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationsErrorService errorService = mock(NotificationsErrorService.class);
        BairroService bairroService = mock(BairroService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(repository.count(any(Specification.class))).thenReturn(12L);
        when(errorService.countForDashboard(request)).thenReturn(3L);

        NotificationService service = new NotificationService(repository, errorService, bairroService);

        assertThat(service.countByIdAgravo(request)).isEqualTo(15L);
    }

    @Test
    void shouldPersistCanonicalRuralZoneAndOriginalLocality() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationsErrorService errorService = mock(NotificationsErrorService.class);
        BairroService bairroService = mock(BairroService.class);
        Bairro rural = new Bairro();
        rural.setId(31L);
        rural.setNome(BairroService.RURAL_NEIGHBORHOOD);
        rural.setCidade("MOSSORO");

        when(errorService.getLastIteration()).thenReturn(0L);
        when(errorService.notificationHasError(any(Notification.class))).thenReturn(false);
        when(bairroService.resolveBairroForNotification("SITIO JATOBA")).thenReturn(Optional.of(rural));

        NotificationDataDTO dto = new NotificationDataDTO(
            1L, "A90", "12/10/2026", "10/10/2026", "01/01/2000", "10", "F",
            "SITIO JATOBA", 0, "1", 26
        );
        NotificationService service = new NotificationService(repository, errorService, bairroService);

        service.saveNotificationsFromBatch(new NotificationBatchDTO(List.of(dto)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(notification -> {
            assertThat(notification.getNomeBairro()).isEqualTo(BairroService.RURAL_NEIGHBORHOOD);
            assertThat(notification.getNomeBairroOriginal()).isEqualTo("SITIO JATOBA");
            assertThat(notification.getIdBairro()).isEqualTo(31);
            assertThat(notification.getDataPrimeiroSintoma()).isBefore(notification.getDataNotification());
        });
    }
}
