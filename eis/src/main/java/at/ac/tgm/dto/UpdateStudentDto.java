package at.ac.tgm.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStudentDto {
    private Long id;
    private String vorname;
    private String nachname;
    private String schuelerkennzahl;
    private Long classroomId; // ID der gewählten Klasse (aus dem Dropdown)
}
