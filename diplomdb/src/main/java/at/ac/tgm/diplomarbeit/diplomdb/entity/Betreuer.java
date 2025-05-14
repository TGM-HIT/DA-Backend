package at.ac.tgm.diplomarbeit.diplomdb.entity;

import jakarta.persistence.*;
import lombok.*;

import static at.ac.tgm.Consts.DIPLOMDB_TABLE_PREFIX;

/**
 * Entität zur Repräsentation eines Betreuers in der Diplomarbeitsdatenbank.
 * Diese Klasse enthält grundlegende Informationen über einen Betreuer,
 * einschließlich seiner Identifikation, Kontaktdaten und Kapazitätsangaben.
 */
@Entity
@Table(name = DIPLOMDB_TABLE_PREFIX + "betreuer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Betreuer {

    /**
     * Eindeutiger Identifikator des Betreuers.
     * Wird automatisch generiert.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Eindeutiger SamAccountName des Betreuers aus dem LDAP-Verzeichnis.
     * Dieser Wert muss einzigartig und nicht null sein.
     */
    @Column(unique = true, nullable = false)
    private String samAccountName;

    /**
     * Vorname des Betreuers.
     */
    private String vorname;

    /**
     * Nachname des Betreuers.
     */
    private String nachname;

    /**
     * E-Mail-Adresse des Betreuers.
     */
    private String email;

    /**
     * Anzeigename des Betreuers.
     */
    private String displayName;

    /**
     * Aktueller Status des Betreuers, beispielsweise "verfügbar" oder "nicht verfügbar".
     */
    private String status;

    /**
     * Maximale Anzahl von Projekten, die der Betreuer betreuen kann.
     * Dieser Wert wird vom Betreuer selbst definiert.
     */
    private Integer maxProjekte;

    /**
     * Anzahl der aktuell zugewiesenen Projekte.
     */
    private Integer vergebeneProjekte;

    /**
     * Berechnet die verbleibende Kapazität des Betreuers, basierend auf der maximalen Projektanzahl
     * und der bereits zugewiesenen Projekte.
     * Diese Information wird nicht in der Datenbank gespeichert.
     *
     * @return Anzahl der Projekte, die noch betreut werden können.
     */
    @Transient
    public int getFreieProjekte() {
        if (maxProjekte == null) return 0;
        if (vergebeneProjekte == null) return maxProjekte;
        return maxProjekte - vergebeneProjekte;
    }
}
