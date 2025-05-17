package at.ac.tgm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HitclassWithTeacherDto {
    private Long id;
    private String name;
    private String klassenvorstand; // Name des Klassenvorstands
    private List<TeacherDto> teachers; // Liste der Lehrer
}