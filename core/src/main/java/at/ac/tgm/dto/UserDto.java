package at.ac.tgm.dto;

import java.util.List;

public record UserDto(
        String username,
        String fullname,
        List<String> roles,
        LoginTypeEnum type
) {
}
