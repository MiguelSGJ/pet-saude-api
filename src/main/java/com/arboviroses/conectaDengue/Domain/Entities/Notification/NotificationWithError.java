package com.arboviroses.conectaDengue.Domain.Entities.Notification;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_error_seq")
    @SequenceGenerator(name = "notifications_error_seq", sequenceName = "notifications_error_seq", allocationSize = 50)
    private Long idNotification;
    private String idAgravo;
    private int idadePaciente;
    private Date dataNotification;
    private Date dataPrimeiroSintoma;
    private Date dataNascimento;
    private String classificacao;
    private String sexo;
    private int idBairro;
    private String nomeBairro;
    private String evolucao;  
    private int semanaEpidemiologica;
    private long iteration;
}
