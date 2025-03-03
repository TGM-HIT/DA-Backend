package at.ac.tgm.diplomarbeit.diplomdb.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class DiplomarbeitDTO {
    private Long projektId;
    private String titel;
    private String beschreibung;
    private String status;
    private LocalDate startdatum;
    private LocalDate enddatum;
    private String ablehnungsgrund;
    private String betreuerSamAccountName;
    private Set<String> mitarbeiterSamAccountNames;
}
