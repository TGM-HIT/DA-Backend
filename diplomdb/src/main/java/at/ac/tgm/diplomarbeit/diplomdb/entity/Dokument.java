package at.ac.tgm.diplomarbeit.diplomdb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import static at.ac.tgm.Consts.DIPLOMDB_TABLE_PREFIX;

@Entity
@Table(name = DIPLOMDB_TABLE_PREFIX + "dokument")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Dokument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dokument_id")
    private Long dokumentId;

    /**
     * Titel des Dokuments.
     */
    private String titel;

    /**
     * Detaillierte Beschreibung des Inhalts oder Zwecks des Dokuments.
     */
    private String beschreibung;

    /**
     * Typ des Dokuments, beispielsweise "Lastenheft" oder "Machbarkeitsstudie".
     */
    private String typ;

    /**
     * Datum, an dem das Dokument hochgeladen wurde.
     */
    private LocalDate hochladungsdatum;

    /**
     * Datum, das im Dokument angegeben oder relevant ist.
     */
    private LocalDate datum;

    /**
     * Originalname der Datei.
     */
    private String dateiname;

    /**
     * Dateipfad, unter dem das Dokument auf dem Server gespeichert ist.
     */
    private String dateipfad;
    private String username;    // Für Tests (optional)

    /**
     * Zuordnung des Dokuments zu einer bestimmten Diplomarbeit.
     */
    @ManyToOne
    @JoinColumn(name = "projekt_id")
    private Diplomarbeit diplomarbeit;

    /**
     * SamAccountName des Erstellers, der das Dokument hochgeladen hat.
     */
    private String erstellerSamAccountName;

    /**
     * Bewertung des Dokuments in Prozent (0 bis 100). Null bedeutet, dass noch keine Bewertung erfolgt ist.
     */
    private Integer bewertungProzent;

    /**
     * SamAccountName des Benutzers, der das Dokument zuletzt bewertet hat.
     */
    private String bewertetDurchSamAccountName;

    /**
     * Kommentar oder Feedback, das bei der Bewertung des Dokuments abgegeben wurde.
     */
    @Column(length = 2000)
    private String bewertungsKommentar;
}
