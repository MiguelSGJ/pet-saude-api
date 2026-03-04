package com.arboviroses.conectaDengue.Domain.Repositories.Notifications;

import org.springframework.data.jpa.domain.Specification;
import com.arboviroses.conectaDengue.Api.DTO.response.AgravoCountBySemanaEpidemiologica;
import com.arboviroses.conectaDengue.Api.DTO.response.BairroCountDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;
import java.util.List;

public interface NotificationRepositoryCustom {
    List<AgravoCountBySemanaEpidemiologica> buscarContagemPorSemanaEpidemiologica(Specification<Notification> spec);
    List<BairroCountDTO> buscarContagemPorBairro(Specification<Notification> spec);
}
