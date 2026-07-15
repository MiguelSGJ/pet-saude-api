package com.arboviroses.conectaDengue.Domain.Services.Notifications;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
            (notification.getDataNotification() == null || notification.getDataPrimeiroSintoma() == null) ||
            notification.getClassificacao() == null ||
            // Idade 999 = não foi possível calcular (sem NU_IDADE_N nem DT_NASC)
            notification.getIdadePaciente() == 999 ||
            notification.getSexo() == null ||
            // Bairro inválido apenas quando o nome também não foi resolvido
            notification.getNomeBairro() == null ||
            notification.getEvolucao() == null
        );
    }

    public void deleteById(Long id) {
        notificationWithErrorRepository.deleteById(id);
    }

    public void deleteByDiseaseAndDataNotification(Map<String, ? extends Collection<Date>> datesByDisease) {
        for (Map.Entry<String, ? extends Collection<Date>> entry : datesByDisease.entrySet()) {
            Collection<Date> dates = entry.getValue();
            if (dates == null || dates.isEmpty()) {
                continue;
            }

            if (entry.getKey() == null) {
                notificationWithErrorRepository.deleteByIdAgravoIsNullAndDataNotificationIn(dates);
            } else {
                notificationWithErrorRepository.deleteByIdAgravoAndDataNotificationIn(entry.getKey(), dates);
            }
        }
    }

    public String categorizeError(NotificationWithError n) {
        if (n.getIdAgravo() == null || n.getIdAgravo().isBlank()) return "DOENCA_NAO_INFORMADA";
        if (n.getNomeBairro() == null || n.getNomeBairro().isBlank()) return "BAIRRO_FALTANDO";
        if (n.getClassificacao() == null || n.getClassificacao().isBlank()) return "CLASSIFICACAO_FALTANDO";
        if (n.getDataNotification() == null || n.getDataPrimeiroSintoma() == null) return "DATA_FALTANDO";
        if (n.getSexo() == null || n.getSexo().isBlank()) return "SEXO_NAO_INFORMADO";
        if (n.getEvolucao() == null || n.getEvolucao().isBlank()) return "EVOLUCAO_NAO_INFORMADA";
        if (n.getIdadePaciente() == 999) return "DATA_NASCIMENTO_FALTANDO";
        return "OUTROS";
    }

    public List<NotificationErrorWithCategoryDTO> getAllErrorsFiltered(String category, Long startDate, Long endDate, String idAgravo) {
        return notificationWithErrorRepository.findAllWithMaxIteration().stream()
            .map(n -> new NotificationErrorWithCategoryDTO(n, categorizeError(n)))
            .filter(n -> category == null || category.isBlank() || category.equals(n.getCategory()))
            .filter(n -> startDate == null || (n.getDataNotification() != null && n.getDataNotification().getTime() >= startDate))
            .filter(n -> endDate == null || (n.getDataNotification() != null && n.getDataNotification().getTime() <= endDate))
            .filter(n -> idAgravo == null || idAgravo.isBlank() || idAgravo.equals(n.getIdAgravo()))
            .sorted((a, b) -> {
                if (a.getDataNotification() == null && b.getDataNotification() == null) return 0;
                if (a.getDataNotification() == null) return 1;
                if (b.getDataNotification() == null) return -1;
                return b.getDataNotification().compareTo(a.getDataNotification());
            })
            .collect(Collectors.toList());
    }

    public Page<NotificationErrorWithCategoryDTO> getAllErrorsPaginated(Pageable pageable, String category, Long startDate, Long endDate, String idAgravo) {
        List<NotificationWithError> all = notificationWithErrorRepository.findAllWithMaxIteration();

        List<NotificationErrorWithCategoryDTO> categorized = all.stream()
            .map(n -> new NotificationErrorWithCategoryDTO(n, categorizeError(n)))
            .filter(n -> category == null || category.isBlank() || category.equals(n.getCategory()))
            .filter(n -> startDate == null || (n.getDataNotification() != null && n.getDataNotification().getTime() >= startDate))
            .filter(n -> endDate == null || (n.getDataNotification() != null && n.getDataNotification().getTime() <= endDate))
            .filter(n -> idAgravo == null || idAgravo.isBlank() || idAgravo.equals(n.getIdAgravo()))
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), categorized.size());
        List<NotificationErrorWithCategoryDTO> pageContent = start >= categorized.size()
            ? List.of()
            : categorized.subList(start, end);

        return new PageImpl<>(pageContent, pageable, categorized.size());
    }
}
