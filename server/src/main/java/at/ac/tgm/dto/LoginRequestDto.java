package at.ac.tgm.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String username; // Email or just the part before @
    private String password;
}
