package at.ac.tgm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonsDto {
    private Long id;
    private String subjectName;
    private String hitclassName;
    public LessonsDto(Long id, String subjectName, String hitclassName) {
        this.id = id;
        this.subjectName = subjectName;
        this.hitclassName = hitclassName;
    }
}
