package at.ac.tgm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherDto {
    private Long id;
    private String name;

    public TeacherDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
