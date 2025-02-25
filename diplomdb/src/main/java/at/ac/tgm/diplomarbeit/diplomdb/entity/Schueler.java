package at.ac.tgm.diplomarbeit.diplomdb.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entität zur Darstellung eines Schülers.
 * Der Schüler wird über seinen SamAccountName eindeutig identifiziert und enthält
 * persönliche Informationen wie Vorname, Nachname, E-Mail-Adresse und Anzeigename.
 */
@Entity
@Table(name = "schueler")
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
