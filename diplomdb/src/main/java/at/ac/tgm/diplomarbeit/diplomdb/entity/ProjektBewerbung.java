package at.ac.tgm.diplomarbeit.diplomdb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entität zur Darstellung einer Bewerbung für ein Projekt.
 * Eine Bewerbung enthält Informationen zum zugehörigen Projekt, dem Bewerber, der Priorität der Bewerbung
 * sowie eventuelle Team-Mitglieder. Die Kombination aus Projekt, Bewerber und Priorität muss eindeutig sein.
 */
@Entity
@Table(name = "projekt_bewerbung",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"projekt_id", "sam_account_name", "prioritaet"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjektBewerbung {

    /**
     * Eindeutiger Identifikator der Bewerbung.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bewerbung_id")
    private Long bewerbungId;

    /**
     * ID des zugehörigen Projekts (Diplomarbeit).
     */
    @Column(name = "projekt_id", nullable = false)
    private Long projektId;

    /**
     * SamAccountName des Haupt-Bewerbers, der die Bewerbung eingereicht hat.
     */
    @Column(name = "sam_account_name", nullable = false)
    private String samAccountName;

    /**
     * Priorität der Bewerbung, beispielsweise 1, 2 oder 3.
     */
    @Column(name = "prioritaet", nullable = false)
    private Integer prioritaet;

    /**
     * Entscheidungsstatus der Bewerbung. Mögliche Werte sind "PENDING", "ACCEPTED" oder "REJECTED".
     * Standardmäßig wird "PENDING" gesetzt.
     */
    @Column(name = "entscheidungs_status", nullable = false)
    private String entscheidungsStatus = "PENDING";

    /**
     * Zusätzliche Team-Mitglieder, die an der Bewerbung beteiligt sind.
     * Diese werden in einer separaten Tabelle gespeichert.
     */
    @ElementCollection
    @CollectionTable(
            name = "projekt_bewerbung_teammitglieder",
            joinColumns = @JoinColumn(name = "bewerbung_id")
    )
    @Column(name = "team_member_sam")
    private Set<String> teamMitglieder = new HashSet<>();
}
