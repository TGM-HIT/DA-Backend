package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "meldung")
@Getter
@Setter
@NoArgsConstructor
public class MeldeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("Welcher Bootstick ist defekt")
    @ManyToOne
    @JoinColumn(name = "bootstick_id", nullable = false)
    private BootstickEntity bootstick;

    @Comment("Nachricht")
    private String nachricht;

    @Comment("Datum der Meldung")
    private LocalDateTime datum;

    @Comment("Wer hat gemeldet")
    @ManyToOne
    @JoinColumn(name = "lehrer_id")
    private LehrerEntity gemeldetVon;
}