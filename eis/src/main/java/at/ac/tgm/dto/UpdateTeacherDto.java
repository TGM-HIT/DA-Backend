package at.ac.tgm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeacherDto {
    private Long id;           // Teacher ID
    private String name;       // optional, wenn du beim Erstellen "name" brauchst
    private List<Long> lessonIds;
}