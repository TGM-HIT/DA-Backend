package at.ac.tgm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemDto {
    private Long id;
    @NotNull
    @NotEmpty
    @Size(min = 2, max = 50)
    private String name;
}
