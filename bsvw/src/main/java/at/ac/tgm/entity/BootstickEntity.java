package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import at.ac.tgm.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "bootstick", uniqueConstraints = @UniqueConstraint(columnNames = {"name","nummer"}))
@Getter
@Setter
@NoArgsConstructor
public class BootstickEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Schulklasse name; //Klassenbuchstabe "A", "B", "C", "D"
    private int nummer; //Bootsticknummer z.B. "1" bit "40"
    //Status des Bootsticks, "VORHANDEN", "AUSGEBORGT, "VERLOREN"
    @Enumerated(EnumType.STRING)
    private Status status;
    //Zustand des Bootsticks, "IN_ORDNUNG", "FEHLERHAFT", "DEFEKT"
    @Enumerated(EnumType.STRING)
    private Zustand zustand;
    //Datum der letzten Initialisierung/Veränderung des Bootsticks
    private LocalDateTime letzte_initialisierung;
}
