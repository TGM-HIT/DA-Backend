package at.ac.tgm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kurzbezeichnung;  // Spalte 14 (z.B. "BPG_AM")
    private String gegenstandsart;   // Spalte 12 (z.B. "Pflichtgegenstände")
    private String langbezeichnung;  // Spalte 15 (z.B. "Angewandte Mathematik")
}