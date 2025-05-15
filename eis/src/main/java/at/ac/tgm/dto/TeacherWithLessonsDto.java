package at.ac.tgm.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherWithLessonsDto {
    private Long id;
    private String name;
    private List<Long> lessonIds;
}
