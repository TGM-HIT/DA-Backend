package at.ac.tgm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static at.ac.tgm.Consts.DIPLOMDB_TABLE_PREFIX;

/**
 * Entität zur Darstellung einer Diplomarbeit bzw. eines Projekts.
 * Diese Klasse enthält alle relevanten Informationen zum Projekt, wie Titel, Beschreibung,
 * Status, Zeitrahmen, zugehöriger Betreuer und beteiligte Mitarbeiter.
 */
@Entity
@Table(name = DIPLOMDB_TABLE_PREFIX + "diplomarbeit")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Diplomarbeit {

    /**
     * Eindeutiger Identifikator des Projekts (Diplomarbeit).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projekt_id")
    private Long projektId;

    /**
     * Titel des Projekts.
     */
    private String titel;

    /**
     * Ausführliche Beschreibung oder Projektdetails.
     */
    private String beschreibung;

    /**
     * Aktueller Status des Projekts, z. B. "EINGEREICHT", "ANGENOMMEN" oder "ABGELEHNT".
     */
    private String status;

    /**
     * Startdatum des Projekts.
     */
    private LocalDate startdatum;

    /**
     * Enddatum des Projekts.
     */
    private LocalDate enddatum;

    /**
     * Ablehnungsgrund, falls das Projekt abgelehnt wurde.
     */
    private String ablehnungsgrund;

    /**
     * SamAccountName des Betreuers, der dem Projekt zugeordnet ist.
     */
    private String betreuerSamAccountName;

    /**
     * Sammlung der SamAccountNames der Mitarbeiter, die an dem Projekt beteiligt sind.
     */
    @ElementCollection
    @CollectionTable(name = "diplomarbeit_mitarbeiter", joinColumns = @JoinColumn(name = "projekt_id"))
    @Column(name = "mitarbeiter_sam_account_name")
    private Set<String> mitarbeiterSamAccountNames = new HashSet<>();

    /**
     * Menge der Meilensteine, die diesem Projekt zugeordnet sind.
     * Diese werden bei der Serialisierung nicht berücksichtigt.
     */
    @OneToMany(mappedBy = "diplomarbeit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Meilenstein> meilensteine = new HashSet<>();
}
