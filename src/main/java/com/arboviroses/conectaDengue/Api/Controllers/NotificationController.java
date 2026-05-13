package com.arboviroses.conectaDengue.Api.Controllers;

import java.util.List;
import java.util.Map;

import com.arboviroses.conectaDengue.Api.DTO.request.UpdateNotificationDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.ManageNotificationResponseDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.NotificationErrorWithCategoryDTO;
import com.arboviroses.conectaDengue.Domain.Services.reports.ErrorsPdfReportService;
import com.arboviroses.conectaDengue.Domain.Services.reports.NeighborhoodWeeklyPdfReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arboviroses.conectaDengue.Api.DTO.request.NeighborhoodWeeklyPdfReportRequest;
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
import com.arboviroses.conectaDengue.Domain.Services.Notifications.NotificationAsyncService;
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
    private final NotificationAsyncService notificationAsyncService;
    private final NotificationsErrorService notificationsErrorService;
    private final NeighborhoodWeeklyPdfReportService neighborhoodWeeklyPdfReportService;
    private final ErrorsPdfReportService errorsPdfReportService;

    @PostMapping("/uploadXlsx")
    public ResponseEntity<SuccessResponseDTO<String>> uploadXlsx(@RequestParam("file") MultipartFile file) throws Exception {
        notificationAsyncService.processXlsxAsync(file.getBytes());
        return ResponseEntity.accepted().body(SuccessResponseDTO.setResponse("processamento iniciado", null));
    }

    @PostMapping("/uploadCsv")
    public ResponseEntity<SuccessResponseDTO<String>> uploadCsv(@RequestParam("file") MultipartFile file) throws Exception {
        notificationAsyncService.processCsvAsync(file.getBytes());
        return ResponseEntity.accepted().body(SuccessResponseDTO.setResponse("processamento iniciado", null));
    }

    @PostMapping("/uploadDbf")
    public ResponseEntity<SuccessResponseDTO<String>> uploadDbf(@RequestParam("file") MultipartFile file) throws Exception {
        notificationAsyncService.processDbfAsync(file.getBytes());
        return ResponseEntity.accepted().body(SuccessResponseDTO.setResponse("processamento iniciado", null));
    }

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

    @GetMapping("/notifications/manage")
    public ResponseEntity<SuccessResponseDTO<Page<ManageNotificationResponseDTO>>> manageNotifications(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer week,
        @RequestParam(required = false) String bairro,
        @RequestParam(required = false) String idAgravo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(SuccessResponseDTO.setResponse(
            notificationService.getNotificationsManage(year, week, bairro, idAgravo, page, size), null));
    }

    @PutMapping("/notifications/{id}")
    public ResponseEntity<SuccessResponseDTO<String>> updateNotification(
        @PathVariable long id, @RequestBody UpdateNotificationDTO dto) throws Exception {
        notificationService.updateNotification(id, dto);
        return ResponseEntity.ok(SuccessResponseDTO.setResponse("Notificação atualizada", null));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<SuccessResponseDTO<String>> deleteNotification(@PathVariable long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(SuccessResponseDTO.setResponse("Notificação removida", null));
    }

    @GetMapping("/notifications/errors/manage")
    public ResponseEntity<SuccessResponseDTO<Page<NotificationErrorWithCategoryDTO>>> manageErrors(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Long startDate,
        @RequestParam(required = false) Long endDate,
        @RequestParam(required = false) String idAgravo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        org.springframework.data.domain.PageRequest pageable =
            org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(SuccessResponseDTO.setResponse(
            notificationsErrorService.getAllErrorsPaginated(pageable, category, startDate, endDate, idAgravo), null));
    }

    @PutMapping("/notifications/errors/{id}")
    public ResponseEntity<SuccessResponseDTO<String>> updateNotificationError(
        @PathVariable long id, @RequestBody UpdateNotificationDTO dto) throws Exception {
        notificationService.updateNotification(id, dto);
        return ResponseEntity.ok(SuccessResponseDTO.setResponse("Registro atualizado", null));
    }

    @DeleteMapping("/notifications/errors/{id}")
    public ResponseEntity<SuccessResponseDTO<String>> deleteNotificationError(@PathVariable long id) {
        notificationsErrorService.deleteById(id);
        return ResponseEntity.ok(SuccessResponseDTO.setResponse("Registro removido da lista de erros", null));
    }

    @GetMapping("/notifications/latest-date")
    public ResponseEntity<SuccessResponseDTO<String>> getLatestNotificationDate() {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.getLatestNotificationDate(), null));
    }

    @GetMapping("/notifications/count/evolucao")
    public ResponseEntity<SuccessResponseDTO<Long>> getEvolucao(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok().body(SuccessResponseDTO.setResponse(notificationService.countByEvolucao(request), null));
    }

    @GetMapping(value = "/notifications/errors/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateErrorsPdfReport(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Long startDate,
        @RequestParam(required = false) Long endDate,
        @RequestParam(required = false) String idAgravo
    ) {
        byte[] pdf = errorsPdfReportService.generateReport(category, startDate, endDate, idAgravo);
        String filename = (category != null && !category.isBlank())
            ? "erros-" + category.toLowerCase() + ".pdf"
            : "erros-todos.pdf";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping(value = "/notifications/report/neighborhood/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateNeighborhoodWeeklyPdfReport(
        @RequestParam("semanaFinal") Integer semanaFinal,
        @RequestParam(required = false) Integer semanaInicial,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String agravo,
        @RequestParam(required = false) String bairro
    ) throws InvalidAgravoException {
        NeighborhoodWeeklyPdfReportRequest reportRequest =
            new NeighborhoodWeeklyPdfReportRequest(semanaInicial, semanaFinal, year, agravo, bairro);

        byte[] pdf = neighborhoodWeeklyPdfReportService.generateReport(reportRequest);

        int inicio = semanaInicial != null ? semanaInicial : 1;
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-bairros-semanas-" + inicio + "-a-" + semanaFinal + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
