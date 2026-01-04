package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import at.ac.tgm.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "datenstick")
@Getter
@Setter
@NoArgsConstructor
public class DatenstickEntity { //TODO anzupassen wenn genaue neue Struktur der Datensticks bekannt
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Zustand zustand;
    private LocalDateTime letzte_initialisierung;
}
