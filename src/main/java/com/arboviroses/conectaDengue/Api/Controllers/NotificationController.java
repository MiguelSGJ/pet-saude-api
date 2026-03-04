package com.arboviroses.conectaDengue.Api.Controllers;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.arboviroses.conectaDengue.Api.DTO.request.NotificationBatchDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.AgravoCountByAgeRange;
import com.arboviroses.conectaDengue.Api.DTO.response.AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse;
import com.arboviroses.conectaDengue.Api.DTO.response.BairroCountDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.CountAgravoBySexoDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.DataNotificationResponseDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.SaveCsvResponseDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.SuccessResponseDTO;
import com.arboviroses.conectaDengue.Api.Exceptions.InvalidAgravoException;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.NotificationWithError;
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationService;
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationsErrorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class NotificationController 
{
    private final NotificationService notificationService;
    private final NotificationsErrorService notificationsErrorService;

    @PostMapping("/saveNotifications")
    public ResponseEntity<SuccessResponseDTO<SaveCsvResponseDTO>> saveNotifications(@RequestBody NotificationBatchDTO notificationsData) throws Exception {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.saveNotificationsFromBatch(notificationsData), "notificações salvas com sucesso"));
    }

    @GetMapping("/notifications/errors")
    public ResponseEntity<SuccessResponseDTO<List<NotificationWithError>>> getAllNotificationsWithError() {
        return ResponseEntity.ok().body(
            SuccessResponseDTO.setResponse(notificationsErrorService.getAllNotificationsWithErrorFromLastIteration(), null)
        );
    }

    @GetMapping("/notifications")
    public ResponseEntity<SuccessResponseDTO<Page<DataNotificationResponseDTO>>> getAll(HttpServletRequest request) throws InvalidAgravoException {
        Pageable pageable = Pageable.ofSize(20);

        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.getNotificationsByIdAgravoPaginated(pageable, request), null));
    }

    @GetMapping("/notifications/count")
    public ResponseEntity<SuccessResponseDTO<Long>> countNotifications(Pageable pageable, HttpServletRequest request) throws Exception {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.countByIdAgravo(request), null));
    }

    @GetMapping("/notifications/count/sexo")
    public ResponseEntity<SuccessResponseDTO<CountAgravoBySexoDTO>> get(HttpServletRequest request) throws InvalidAgravoException {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.getNotificationsInfoBySexo(request), null));
    }

    @PostMapping("/notifications/count/byYears")
    public ResponseEntity<Map<Integer, Map<Integer, Long>>> getNotificationCountsByYear(@RequestBody List<Integer> years) {
        Map<Integer, Map<Integer, Long>> result = notificationService.getNotificationCountsByYear(years);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/notifications/count/epidemiologicalWeek")
    public ResponseEntity<SuccessResponseDTO<AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse>> getSemana(HttpServletRequest request) throws InvalidAgravoException {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.countNotificationsBySemanaEpidemiologica(request), null));
    }

    @GetMapping("/notifications/count/epidemiologicalWeek/accumulated")
    public ResponseEntity<SuccessResponseDTO<AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse>> getSemanaAccumulated(HttpServletRequest request) throws InvalidAgravoException {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.countNotificationsBySemanaEpidemiologicaAccumulated(request), null));
    }

    @GetMapping("/notifications/count/ageRange")
    public ResponseEntity<SuccessResponseDTO<AgravoCountByAgeRange>> getByAgeRange(HttpServletRequest request) throws InvalidAgravoException {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.getNotificationsCountByAgeRange(request), null));
    }

    @GetMapping("/notifications/count/neighborhood")
    public ResponseEntity<SuccessResponseDTO<List<BairroCountDTO>>> getBairro(HttpServletRequest request) throws InvalidAgravoException {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.getBairroCount(request), null));
    }

    @GetMapping("/notifications/count/evolucao")
    public ResponseEntity<SuccessResponseDTO<Long>> getEvolucao(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.countByEvolucao(request), null));
    }
}