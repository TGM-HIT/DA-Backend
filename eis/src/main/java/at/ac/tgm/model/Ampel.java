package at.ac.tgm.model;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = Consts.EIS_TABLE_PREFIX + "AMPELEIS")
public class Ampel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Lesson lesson;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    private AmpelFarbe farbe;

    private String bemerkung;
    private LocalDateTime updatedAt;
}


