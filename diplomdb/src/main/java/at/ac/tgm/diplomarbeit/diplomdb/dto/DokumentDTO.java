package at.ac.tgm.diplomarbeit.diplomdb.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DokumentDTO {

    private Long dokumentId;
    private String titel;
    private String beschreibung;
    private String typ;
    private LocalDate hochladungsdatum;
    private LocalDate datum;
    private String dateiname;
    private Long diplomarbeitId;
    private String diplomarbeitTitel;
    private String erstellerSamAccountName;

    /**
     * Bewertung in Prozent (0-100). Null bedeutet noch nicht bewertet.
     */
    private Integer bewertungProzent;

    /**
     * Wer hat das Dokument zuletzt bewertet (Teacher/Admin)?
     */
    private String bewertetDurchSamAccountName;

    /**
     * Kommentar/Feedback des Lehrers bei der Bewertung
     */
    private String bewertungsKommentar;
}
