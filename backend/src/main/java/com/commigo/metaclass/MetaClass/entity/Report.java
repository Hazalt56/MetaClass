package com.commigo.metaclass.MetaClass.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Il numero di partecipanti non può essere nullo")
    @Min(value = 1, message = "Il numero di partecipanti non può essere inferiore a 1")
    private int num_Partecipanti = 1;

    @Column(name = "Durata_Meeting")
    @NotNull(message = "La durata del meeting non può essere nulla")
    private Duration durataMeeting;

    @Column(name = "MAX_Partecipanti")
    @NotNull(message = "Il numero massimo di partecipanti non può essere nullo")
    @Min(value = 1, message = "Il numero massimo di partecipanti non può essere inferiore a 1")
    private int max_Partecipanti = 1;

    @NotNull(message = "Il meeting non può essere nullo")
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "id_meeting")
    private Meeting meeting;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<Utente> lista_partecipanti;

    @Column(name = "Data_Creazione", updatable = false)
    @CreationTimestamp
    private LocalDateTime data_Creazione;

    @Column(name = "Data_Aggiornamento")
    @UpdateTimestamp
    private LocalDateTime data_Aggiornamento;

    //costruttore richiamato all'avvio del meeting
    public Report (Meeting meeting, Utente ogm){
        this.meeting = meeting;
        this.lista_partecipanti = List.of(ogm);
        this.durataMeeting = Duration.ZERO;
    }

}
