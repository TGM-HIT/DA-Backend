package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "rueckgabe")
@Getter
@Setter
@NoArgsConstructor
public class RueckgabeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("Zugehörige Ausleihe")
    @OneToOne
    @JoinColumn(name = "ausleihe_id", nullable = false)
    private AusleiheEntity ausleihe;

    @Comment("Nachricht der Rückgabe")
    private String nachricht;

    @Comment("Datum der Rückgabe")
    private LocalDateTime rueckgabedatum;
}