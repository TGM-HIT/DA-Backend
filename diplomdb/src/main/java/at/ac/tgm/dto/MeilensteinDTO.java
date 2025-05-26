package at.ac.tgm.dto;

import lombok.Data;

@Data
public class MeilensteinDTO {

    private Long meilensteinId;
    private String name;       // Name des Meilensteins
    private String status;     // OFFEN, IN_BEARBEITUNG, ERFUELLT
    private Long projektId;    // ID der Diplomarbeit
}
