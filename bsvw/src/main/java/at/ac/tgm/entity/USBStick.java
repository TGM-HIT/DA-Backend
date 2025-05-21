package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "usb_stick")
@Getter
@Setter
@NoArgsConstructor
public class USBStick {

    @Id
    private String inventarnummer;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonBackReference
    private StickGroup group;

    private String typ;
    private String speicherkapazitaet;
    private String hersteller;
    private String modell;
    private String seriennummer;
    private String verfuegbarkeit;
    private String zustand;
}
