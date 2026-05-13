package com.arboviroses.conectaDengue.Domain.Services.Notifications;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.arboviroses.conectaDengue.Api.DTO.response.NotificationErrorWithCategoryDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.NotificationWithError;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationWithErrorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationsErrorService {
    private final NotificationWithErrorRepository notificationWithErrorRepository;

    public List<NotificationWithError> getAllNotificationsWithErrorFromLastIteration() {
        return notificationWithErrorRepository.findAllWithMaxIteration();
    }

    public NotificationWithError getNotificationWithErrorById(Long id) {
        return notificationWithErrorRepository.findById(id).orElse(null);
    }

    public void saveNotificationWithError(NotificationWithError notificationWithError) {
        notificationWithErrorRepository.save(notificationWithError);
    }

    public void deleteAllNotificationsWithError() {
        notificationWithErrorRepository.deleteAll();
    }

    public List<NotificationWithError> insertListOfNotifications(List<NotificationWithError> notifications) {
        return notificationWithErrorRepository.saveAll(notifications);
    }

    public Long getLastIteration() {
        NotificationWithError lastNotificationWithError = notificationWithErrorRepository.findTopByOrderByIterationDesc().orElse(null);
        return lastNotificationWithError != null ? lastNotificationWithError.getIteration() : 0L;
    }

    public boolean notificationHasError(Notification notification) {
        return (
            notification.getIdAgravo() == null ||
            notification.getDataNotification() == null ||
            notification.getClassificacao() == null ||
            notification.getIdadePaciente() == 0 ||
            notification.getSexo() == null ||
            (notification.getIdBairro() == 0 || notification.getNomeBairro() == null) ||
            notification.getEvolucao() == null
        );
    }

    public void deleteById(Long id) {
        notificationWithErrorRepository.deleteById(id);
    }

    public String categorizeError(NotificationWithError n) {
        if (n.getIdAgravo() == null || n.getIdAgravo().isBlank()) return "DOENCA_NAO_INFORMADA";
        if (n.getNomeBairro() == null || n.getNomeBairro().isBlank()) return "BAIRRO_FALTANDO";
        if (n.getIdBairro() == 0) return "ID_BAIRRO_FALTANDO";
        if (n.getClassificacao() == null || n.getClassificacao().isBlank()) return "CLASSIFICACAO_FALTANDO";
        if (n.getDataNotification() == null) return "DATA_FALTANDO";
        if (n.getSexo() == null || n.getSexo().isBlank()) return "SEXO_NAO_INFORMADO";
        if (n.getEvolucao() == null || n.getEvolucao().isBlank()) return "EVOLUCAO_NAO_INFORMADA";
        return "OUTROS";
    }

    public Page<NotificationErrorWithCategoryDTO> getAllErrorsPaginated(Pageable pageable, String category) {
        List<NotificationWithError> all = notificationWithErrorRepository.findAllWithMaxIteration();

        List<NotificationErrorWithCategoryDTO> categorized = all.stream()
            .map(n -> new NotificationErrorWithCategoryDTO(n, categorizeError(n)))
            .filter(n -> category == null || category.isBlank() || category.equals(n.getCategory()))
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), categorized.size());
        List<NotificationErrorWithCategoryDTO> pageContent = start >= categorized.size()
            ? List.of()
            : categorized.subList(start, end);

        return new PageImpl<>(pageContent, pageable, categorized.size());
    }
}
