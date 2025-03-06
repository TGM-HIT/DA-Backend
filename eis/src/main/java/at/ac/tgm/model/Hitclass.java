package at.ac.tgm.model;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = Consts.EIS_TABLE_PREFIX + "HITCLASSEIS")
public class Hitclass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // z.B. "5CHITM"

    @ManyToOne(cascade = CascadeType.ALL) // Add cascade here
    private Teacher klassenvorstand;

    @OneToMany(mappedBy = "hitclass", cascade = CascadeType.ALL) // Add cascade here
    private Set<Student> students = new HashSet<>();

    @OneToMany(mappedBy = "hitclass", cascade = CascadeType.ALL) // Add cascade here
    private Set<Lesson> lessons = new HashSet<>();
}
