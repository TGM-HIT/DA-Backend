package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "meldung")
@Getter
@Setter
@NoArgsConstructor
public class MeldeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Welcher Bootstick ist defekt
    @ManyToOne
    @JoinColumn(name = Consts.BSVW_TABLE_PREFIX +"bootstick_id", nullable = false)
    private BootstickEntity bootstick;

    // Nachricht
    private String nachricht;

    // Datum Meldung
    private LocalDateTime datum;

    // Wer hat gemeldet
    @ManyToOne
    @JoinColumn(name = "lehrer_id")
    private LehrerEntity gemeldetVon;
}