package at.ac.tgm.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AmpelDto {
    private Long ampelId;
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private String subjectKurzbezeichnung;
    private String subjectLangbezeichnung;
    private String gegenstandsart;
    private String hitclassName;
    private String farbe;
    private String bemerkung;
    private LocalDateTime updatedAt;
    private Long lessonId;
}
