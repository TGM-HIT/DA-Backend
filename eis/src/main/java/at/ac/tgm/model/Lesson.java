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
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referenz auf Subject
    @ManyToOne() // Add cascade here
    private Subject subject;

    // Referenz auf Classroom
    @ManyToOne() // Add cascade here
    private Classroom classroom;

    // Ein Unterricht kann mehrere Lehrer haben
    // und umgekehrt kann ein Lehrer mehrere Unterrichtseinheiten abdecken
    @ManyToMany
    @JoinTable(
            name = "lesson_teacher",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private Set<Teacher> teachers = new HashSet<>();
}

