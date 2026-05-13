package com.arboviroses.conectaDengue.Api.DTO.response;

import java.util.Date;
import com.arboviroses.conectaDengue.Domain.Entities.Notification.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManageNotificationResponseDTO {
    private long idNotification;
    private String idAgravo;
    private Date dataNotification;
    private Date dataNascimento;
    private String classificacao;
    private String sexo;
    private int idBairro;
    private String nomeBairro;
    private String evolucao;
    private int idadePaciente;
    private int semanaEpidemiologica;

    public ManageNotificationResponseDTO(Notification n) {
        this.idNotification = n.getIdNotification();
        this.idAgravo = n.getIdAgravo();
        this.dataNotification = n.getDataNotification();
        this.dataNascimento = n.getDataNascimento();
        this.classificacao = n.getClassificacao();
        this.sexo = n.getSexo();
        this.idBairro = n.getIdBairro();
        this.nomeBairro = n.getNomeBairro();
        this.evolucao = n.getEvolucao();
        this.idadePaciente = n.getIdadePaciente();
        this.semanaEpidemiologica = n.getSemanaEpidemiologica();
    }
}
