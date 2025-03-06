package at.ac.tgm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private String firstName;
    private String lastName;
    private String studentKennzahl;
    private String hitclass;
    private Long id;
}
