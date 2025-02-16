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
public class UpdateTeacherDto {
    private Long id;           // Teacher ID
    private String name;       // optional, wenn du beim Erstellen "name" brauchst
    private List<Long> lessonIds;
}