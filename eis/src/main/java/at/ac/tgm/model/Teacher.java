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
@Table(name = Consts.EIS_TABLE_PREFIX + "TEACHEREIS")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "teachers")
    private Set<Lesson> lessons = new HashSet<>();
}

