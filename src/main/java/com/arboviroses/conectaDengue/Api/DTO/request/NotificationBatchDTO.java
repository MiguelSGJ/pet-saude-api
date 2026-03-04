package com.arboviroses.conectaDengue.Api.DTO.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBatchDTO {
    @NotEmpty(message = "A lista de notificações não pode estar vazia")
    private List<NotificationDataDTO> notifications;
}
