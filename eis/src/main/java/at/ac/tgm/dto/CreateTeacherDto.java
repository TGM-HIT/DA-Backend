package at.ac.tgm.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTeacherDto {
    private String name;
    private List<Long> lessonIds;
}

