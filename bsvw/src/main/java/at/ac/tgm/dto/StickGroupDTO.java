package at.ac.tgm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StickGroupDTO {
    private String groupId;
    private String stickType;
    private int numberOfSticks;
}
