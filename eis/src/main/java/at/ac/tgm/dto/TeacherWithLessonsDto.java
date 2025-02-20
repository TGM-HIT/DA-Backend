package at.ac.tgm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherWithLessonsDto {
    private Long id;
    private String name;
    private List<Long> lessonIds;
}
