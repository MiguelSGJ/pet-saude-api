package com.arboviroses.conectaDengue.Domain.Entities.Notification;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "notifications_with_error")
public class NotificationWithError {
    @Id
    private long idNotification;
    private String idAgravo;
    private int idadePaciente;
    private Date dataNotification;
    private Date dataNascimento;
    private String classificacao;
    private String sexo;
    private int idBairro;
    private String nomeBairro;
    private String evolucao;  
    private int semanaEpidemiologica;
    private long iteration;
}
