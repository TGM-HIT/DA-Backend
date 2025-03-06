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
@Table(name = Consts.EIS_TABLE_PREFIX + "EISSTUDENT")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String vorname;
    private String nachname;
    private String studentKennzahl; // spalte 5

    @ManyToOne // Add cascade here
    private Hitclass hitclass;
}

