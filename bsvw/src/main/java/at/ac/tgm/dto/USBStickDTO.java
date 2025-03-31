package at.ac.tgm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class USBStickDTO {
    private String inventarnummer;
    private String groupId;
    private String typ;
    private String speicherkapazitaet;
    private String hersteller;
    private String modell;
    private String seriennummer;
    private String verfuegbarkeit;
    private String zustand;
}
