package com.arboviroses.conectaDengue.Domain.Services.Notifications;

import java.util.List;

import org.springframework.stereotype.Service;

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
            notification.getDataNascimento() == null ||
            notification.getDataNotification() == null ||
            notification.getClassificacao() == null ||
            notification.getIdadePaciente() == 0 ||
            notification.getSexo() == null ||
            (notification.getIdBairro() == 0 || notification.getNomeBairro() == null) ||
            notification.getEvolucao() == null
        );
    }
}
