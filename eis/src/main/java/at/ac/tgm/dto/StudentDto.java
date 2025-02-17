package at.ac.tgm.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StudentDto {
    private String firstName;
    private String lastName;
    private String studentKennzahl;
    private String hitclass;
    private Long id;

    public StudentDto(String firstName, String lastName, String studentKennzahl, String hitclass, Long id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentKennzahl = studentKennzahl;
        this.hitclass = hitclass;
        this.id = id;
    }

}
