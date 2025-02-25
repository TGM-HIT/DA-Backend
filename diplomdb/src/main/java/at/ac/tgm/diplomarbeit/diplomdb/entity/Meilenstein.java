package at.ac.tgm.diplomarbeit.diplomdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entität zur Darstellung eines Meilensteins innerhalb eines Projekts.
 * Ein Meilenstein repräsentiert einen wichtigen Schritt oder eine Phase im Projektverlauf.
 */
@Entity
@Table(name = "meilenstein")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Meilenstein {

    /**
     * Eindeutiger Identifikator des Meilensteins.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meilenstein_id")
    private Long meilensteinId;

    /**
     * Bezeichnung oder Name des Meilensteins.
     */
    private String name;

    /**
     * Aktueller Status des Meilensteins, z. B. "OFFEN", "IN_BEARBEITUNG" oder "ERFUELLT".
     * Standardmäßig wird der Status "OFFEN" gesetzt.
     */
    private String status = "OFFEN";

    /**
     * Zugehörige Diplomarbeit bzw. das Projekt, zu dem dieser Meilenstein gehört.
     * Diese Beziehung wird beim Serialisieren ausgeschlossen.
     */
    @ManyToOne
    @JoinColumn(name = "projekt_id")
    @JsonIgnore
    private Diplomarbeit diplomarbeit;
}
