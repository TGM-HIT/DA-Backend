package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import at.ac.tgm.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "ausleihe")
@Getter
@Setter
@NoArgsConstructor
public class AusleiheEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Tabellenbeziehung der an der Ausleihe beteiligten Lehrer
    @ManyToMany
    @JoinTable(name = "ausleihe_lehrer", joinColumns = @JoinColumn(name = "ausleihe_id"), inverseJoinColumns = @JoinColumn(name = "lehrer_id"))
    private Set<LehrerEntity> lehrer = new HashSet<>();

    // Bootsticks
    @ManyToMany
    @JoinTable(
            name = "ausleihe_bootstick",
            joinColumns = @JoinColumn(name = "ausleihe_id"),
            inverseJoinColumns = @JoinColumn(name = "bootstick_id")
    )
    private Set<BootstickEntity> bootsticks = new HashSet<>();

    // Datensticks
    @ManyToMany
    @JoinTable(
            name = "ausleihe_datenstick",
            joinColumns = @JoinColumn(name = "ausleihe_id"),
            inverseJoinColumns = @JoinColumn(name = "datenstick_id")
    )
    private Set<DatenstickEntity> datensticks = new HashSet<>();
    //Grund/Nachricht der Ausleihe
    private String nachricht;
    //Klassen, für die die zugehörigen Bootstickboxen ausgeliehen werden (A,B,C,D)
    @ElementCollection(targetClass = Schulklasse.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "ausleihe_klassen", joinColumns = @JoinColumn(name = "ausleihe_id"))
    private Set<Schulklasse> klassen = new HashSet<>();
    //Datum der Ausleihe
    private LocalDateTime ausleihedatum;
    //Datum der Rückgabe (Wird nach rückgabe Befüllt)
    private LocalDateTime rueckgabedatum;
}
