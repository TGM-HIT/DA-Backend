package at.ac.tgm.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class DiplomarbeitResponseDTO {

    private Long projektId;
    private String titel;
    private String beschreibung;
    private String status;
    private LocalDate startdatum;
    private LocalDate enddatum;
    private String betreuerSamAccountName;
    private Set<String> mitarbeiterSamAccountNames;
    private String ablehnungsgrund;
}
