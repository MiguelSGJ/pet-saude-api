package com.arboviroses.conectaDengue.Api.DTO.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBatchDTO {
    @Valid
    @NotEmpty(message = "A lista de notificacoes nao pode estar vazia")
    @Size(max = 50000, message = "Lote de notificacoes muito grande")
    private List<NotificationDataDTO> notifications;
}
