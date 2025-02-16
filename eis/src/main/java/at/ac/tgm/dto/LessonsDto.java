package at.ac.tgm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonsDto {
    private Long id;
    private String subjectName;
    private String classRoomName;
    public LessonsDto(Long id, String subjectName, String classRoomName) {
        this.id = id;
        this.subjectName = subjectName;
        this.classRoomName = classRoomName;
    }
}
