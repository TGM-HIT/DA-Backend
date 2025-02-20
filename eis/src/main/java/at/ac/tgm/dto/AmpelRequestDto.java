package at.ac.tgm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class AmpelRequestDto {
    private Long lessonId;
    private Long studentId;
    private Long teacherId;
    private String farbe;     // "ROT", "GELB", "GRUEN", "SCHWARZ", "GRAU"
    private String bemerkung;
}

