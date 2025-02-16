package at.ac.tgm.model;

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
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // z.B. "5CHITM"

    @ManyToOne(cascade = CascadeType.ALL) // Add cascade here
    private Teacher klassenvorstand;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL) // Add cascade here
    private Set<Student> students = new HashSet<>();

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL) // Add cascade here
    private Set<Lesson> lessons = new HashSet<>();
}
