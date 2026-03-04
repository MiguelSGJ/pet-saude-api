package com.arboviroses.conectaDengue.Domain.Services.Notifications;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.arboviroses.conectaDengue.Utils.ConvertNameToIdAgravo;
import com.arboviroses.conectaDengue.Utils.StringToDateCSV;
import com.arboviroses.conectaDengue.Utils.MossoroData.NeighborhoodsMossoro;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.arboviroses.conectaDengue.Api.DTO.request.NotificationBatchDTO;
import com.arboviroses.conectaDengue.Api.DTO.request.NotificationDataDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.AgravoCountByAgeRange;
import com.arboviroses.conectaDengue.Api.DTO.response.AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse;
import com.arboviroses.conectaDengue.Api.DTO.response.BairroCountDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.CountAgravoBySexoDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.DataNotificationResponseDTO;
import com.arboviroses.conectaDengue.Api.DTO.response.SaveCsvResponseDTO;
import com.arboviroses.conectaDengue.Api.Exceptions.InvalidAgravoException;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.NotificationWithError;
import com.arboviroses.conectaDengue.Domain.Filters.NotificationFilters;
import com.arboviroses.conectaDengue.Domain.Repositories.Notifications.NotificationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationsErrorService notificationsErrorService;
    @PersistenceContext
    private EntityManager entityManager;

    public SaveCsvResponseDTO saveNotificationsFromBatch(NotificationBatchDTO notificationBatchDTO) {
        List<Notification> notifications = new ArrayList<>();
        List<NotificationWithError> notificationsWithError = new ArrayList<>();
        Long currentIteration = notificationsErrorService.getLastIteration() + 1;

        for (NotificationDataDTO dto : notificationBatchDTO.getNotifications()) {
            try {
                Notification notification = convertDtoToNotification(dto);
                
                if (notificationsErrorService.notificationHasError(notification)) {
                    NotificationWithError notificationWithError = NotificationWithError.builder()
                        .idNotification(notification.getIdNotification())
                        .idAgravo(notification.getIdAgravo())
                        .idadePaciente(notification.getIdadePaciente())
                        .dataNotification(notification.getDataNotification())
                        .dataNascimento(notification.getDataNascimento())
                        .classificacao(notification.getClassificacao())
                        .sexo(notification.getSexo())
                        .idBairro(notification.getIdBairro())
                        .nomeBairro(NeighborhoodsMossoro.search(notification.getNomeBairro()))
                        .evolucao(notification.getEvolucao())
                        .semanaEpidemiologica(notification.getSemanaEpidemiologica())
                        .iteration(currentIteration)
                        .build();

                    notificationsWithError.add(notificationWithError);
                } 
                
                notifications.add(notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
        
        if (!notificationsWithError.isEmpty()) {
            notificationsErrorService.insertListOfNotifications(notificationsWithError);
        }

        return new SaveCsvResponseDTO(true);
    }

    private Notification convertDtoToNotification(NotificationDataDTO dto) throws ParseException {
        Notification notification = new Notification();
        notification.setIdNotification(dto.getNuNotific());
        notification.setIdAgravo(dto.getIdAgravo());
        notification.setIdadePaciente(dto.getIdade());
        notification.setClassificacao(dto.getClassiFin());
        notification.setSexo(dto.getCsSexo());
        notification.setIdBairro(dto.getIdBairro());
        notification.setNomeBairro(dto.getNmBairro());
        notification.setEvolucao(dto.getEvolucao());

        Date dataNascimento = StringToDateCSV.ConvertStringToDate(dto.getDtNasc());
        Date dataNotification = StringToDateCSV.ConvertStringToDate(dto.getDtNotific());
        
        notification.setDataNascimento(dataNascimento);
        notification.setDataNotification(dataNotification);
        
        if (notification.getDataNotification() != null) {
            notification.setSemanaEpidemiologica(calculateSemanaEpidemiologica(notification.getDataNotification()));
        }
        
        return notification;
    }

    private Integer calculateSemanaEpidemiologica(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }
 

    public Page<DataNotificationResponseDTO> getAllNotificationsPaginated(Pageable pageable)
    {
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        return notifications.map(DataNotificationResponseDTO::new);
    }

    public Page<DataNotificationResponseDTO> getNotificationsByIdAgravoPaginated(Pageable pageable, HttpServletRequest request) throws InvalidAgravoException
    {
        if (request.getParameter("agravo") == null) {
            return getAllNotificationsPaginated(pageable);
        }
        
        String agravo = ConvertNameToIdAgravo.convert(request.getParameter("agravo"));

        Page<Notification> notifications = notificationRepository.findByIdAgravo(pageable, agravo);
        return notifications.map(DataNotificationResponseDTO::new);
    }

    public CountAgravoBySexoDTO getNotificationsInfoBySexo(HttpServletRequest request) throws InvalidAgravoException 
    {
        return NotificationFilters.filtersForNotificationsInfoBySexo(request, notificationRepository);
    }

    public AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse countNotificationsBySemanaEpidemiologica(HttpServletRequest request) throws InvalidAgravoException
    {
        return new AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse(NotificationFilters.filtersForNotificationsInfoBySemanaEpidemiologica(request, notificationRepository)); 
    }

    public AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse countNotificationsBySemanaEpidemiologicaAccumulated(HttpServletRequest request) throws InvalidAgravoException
    {
        return new AgravoCountByEpidemiologicalSemanaEpidemiologicaResponse(NotificationFilters.filtersForNotificationsInfoBySemanaEpidemiologica(request, notificationRepository), true); 
    }

    public AgravoCountByAgeRange getNotificationsCountByAgeRange(HttpServletRequest request) throws InvalidAgravoException {
        return new AgravoCountByAgeRange(NotificationFilters.filtersForNotificationsByAgeRange(request, this.entityManager));
    }

    public List<BairroCountDTO> getBairroCount(HttpServletRequest request) throws InvalidAgravoException 
    {   
        return NotificationFilters.filtersForNotificationsCountNeighborhoods(request, notificationRepository);
    }

    public long countByEvolucao(HttpServletRequest request) throws Exception {
        return NotificationFilters.filterForCountByEvolucao(request, notificationRepository);
    }

    public long countByIdAgravo(HttpServletRequest request) throws Exception {
        return NotificationFilters.filterForCountByIdAgravo(request, notificationRepository);
    }

    public Map<Integer, Map<Integer, Long>> getNotificationCountsByYear(List<Integer> years) {
        Map<Integer, Map<Integer, Long>> result = new HashMap<>();

        for (Integer year : years) {
            List<Notification> notificationsForYear = notificationRepository.findByYearAndIdAgravo(year, "A90");
            Map<Integer, Long> notificationsByMonth = new HashMap<>();

            for (Notification notification : notificationsForYear) {
                int month = getMonth(notification.getDataNotification());
                notificationsByMonth.merge(month, 1L, Long::sum);
            }

            result.put(year, notificationsByMonth);
        }

        return result;
    }

    private int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }
}
