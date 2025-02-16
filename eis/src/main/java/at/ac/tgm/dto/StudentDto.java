package at.ac.tgm.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StudentDto {
    private String firstName;
    private String lastName;
    private String schuelerkennzahl;
    private String classroom;
    private Long id;

    public StudentDto(String firstName, String lastName, String schuelerkennzahl, String classroom, Long id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.schuelerkennzahl = schuelerkennzahl;
        this.classroom = classroom;
        this.id = id;
    }

}
