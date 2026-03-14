package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "rueckgabe")
@Getter
@Setter
@NoArgsConstructor
public class RueckgabeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Zugehörige Ausleihe
    @OneToOne
    @JoinColumn(name = Consts.BSVW_TABLE_PREFIX +"ausleihe_id", nullable = false)
    private AusleiheEntity ausleihe;

    // Nachricht der Rückgabe
    private String nachricht;

    // Datum der Rückgabe
    private LocalDateTime rueckgabedatum;
}