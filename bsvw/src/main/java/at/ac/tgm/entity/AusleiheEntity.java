package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Comment;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "ausleihe")
@Getter
@Setter
@NoArgsConstructor
public class AusleiheEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("Tabellenbeziehung der an der Ausleihe beteiligten Lehrer")
    @ManyToMany
    @JoinTable(name = Consts.BSVW_TABLE_PREFIX +"ausleihe_lehrer", joinColumns = @JoinColumn(name = "ausleihe_id"), inverseJoinColumns = @JoinColumn(name = "lehrer_id"))
    private Set<LehrerEntity> lehrer = new HashSet<>();

    @Comment("Bootsticks")
    @ManyToMany
    @JoinTable(
            name = Consts.BSVW_TABLE_PREFIX +"ausleihe_bootstick",
            joinColumns = @JoinColumn(name = "ausleihe_id"),
            inverseJoinColumns = @JoinColumn(name = "bootstick_id")
    )
    private Set<BootstickEntity> bootsticks = new HashSet<>();

    @Comment("Datensticks")
    @ManyToMany
    @JoinTable(
            name = Consts.BSVW_TABLE_PREFIX +"ausleihe_datenstick",
            joinColumns = @JoinColumn(name = "ausleihe_id"),
            inverseJoinColumns = @JoinColumn(name = "datenstick_id")
    )
    private Set<DatenstickEntity> datensticks = new HashSet<>();
    @Comment("Grund/Nachricht der Ausleihe")
    private String nachricht;
    @Comment("Datum der Ausleihe")
    private LocalDateTime ausleihedatum;
    @Comment("Datum der Rückgabe (Wird nach rückgabe Befüllt)")
    private LocalDateTime rueckgabedatum;
}
