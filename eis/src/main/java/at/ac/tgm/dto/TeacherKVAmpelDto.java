package at.ac.tgm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherKVAmpelDto {
    private Long studentId;
    private String studentName;
    private String schuelerkennzahl;
    private List<AmpelDto> ampelEntries;

}
