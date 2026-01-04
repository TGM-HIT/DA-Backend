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
@Table(name = Consts.BSVW_TABLE_PREFIX + "rueckgabe")
@Getter
@Setter
@NoArgsConstructor
public class RueckgabeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Nachricht der Rückgabe
    private String nachricht;
    //Klassen, von denen die Sticks zurückgegeben worden sind
    @ElementCollection(targetClass = Schulklasse.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "ausleihe_klassen", joinColumns = @JoinColumn(name = "ausleihe_id"))
    private Set<Schulklasse> klassen = new HashSet<>();
    //Datum, wann die Rückgabe stattgefunden hat
    private LocalDateTime rueckgabedatum;
}
