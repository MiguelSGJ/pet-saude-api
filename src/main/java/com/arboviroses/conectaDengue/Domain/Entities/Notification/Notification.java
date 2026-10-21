package com.arboviroses.conectaDengue.Domain.Entities.Notification;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.arboviroses.conectaDengue.Domain.Entities.Weather.WeatherDaily;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "notifications")
@Entity(name = "Notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_seq")
    @SequenceGenerator(name = "notifications_seq", sequenceName = "notifications_seq", allocationSize = 50)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_data_id")
    private WeatherDaily weatherData;
}
