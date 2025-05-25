package at.ac.tgm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import static at.ac.tgm.Consts.DIPLOMDB_TABLE_PREFIX;

/**
 * Entität zur Darstellung eines Schülers.
 * Der Schüler wird über seinen SamAccountName eindeutig identifiziert und enthält
 * persönliche Informationen wie Vorname, Nachname, E-Mail-Adresse und Anzeigename.
 */
@Entity
@Table(name = DIPLOMDB_TABLE_PREFIX + "schueler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schueler {

    /**
     * Primärschlüssel, der den SamAccountName des Schülers repräsentiert.
     */
    @Id
    @Column(name = "sam_account_name", nullable = false, unique = true)
    private String samAccountName;

    /**
     * Vorname des Schülers.
     */
    private String vorname;

    /**
     * Nachname des Schülers.
     */
    private String nachname;

    /**
     * E-Mail-Adresse des Schülers.
     */
    private String email;

    /**
     * Anzeigename des Schülers.
     */
    private String displayName;
}
