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
    private String studentKennzahl;
    private Long hitclassId; // ID der gewählten Klasse (aus dem Dropdown)
}
