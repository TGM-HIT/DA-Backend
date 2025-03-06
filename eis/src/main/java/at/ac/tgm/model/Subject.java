package at.ac.tgm.model;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = Consts.EIS_TABLE_PREFIX + "SUBJECTEIS")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kurzbezeichnung;  // Spalte 14 (z.B. "BPG_AM")
    private String gegenstandsart;   // Spalte 12 (z.B. "Pflichtgegenstände")
    private String langbezeichnung;  // Spalte 15 (z.B. "Angewandte Mathematik")
}