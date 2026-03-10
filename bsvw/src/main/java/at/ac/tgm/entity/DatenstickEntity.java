package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import at.ac.tgm.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "datenstick", uniqueConstraints = @UniqueConstraint(columnNames = {"name","nummer"}))
@Getter
@Setter
@NoArgsConstructor
public class DatenstickEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Schulklasse name; //Klassenbuchstabe "A", "B", "C", "D"
    private int nummer; //Datensticknummer z.B. "1" bit "40"
    //Status des Datensticks, "VORHANDEN", "AUSGEBORGT, "VERLOREN"
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Zustand zustand;
    private LocalDateTime letzte_initialisierung;
}
