package at.ac.tgm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonsDto {
    private Long id;
    private String subjectName;
    private String hitclassName;
}
