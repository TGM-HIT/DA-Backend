package at.ac.tgm.api;

import at.ac.tgm.dto.LoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
public interface AuthenticationApi {
    @PostMapping("/login")
    @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
            @Content(examples = {@ExampleObject(name = "Simulate Teacher Login", value = """
                                    {"username":"mpointner", "password":"", "simulate":true}""")
            }, schema = @Schema(implementation = LoginRequestDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
    }))
    ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response);

    @GetMapping("/csrf-token")
    @Operation(summary = "The CSRF-Token is returned on any call as cookie but if you want to get it explicitly in the body, you can do so with this endpoint")
    CsrfToken csrfToken(HttpServletRequest request);

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session);

    @GetMapping({"", "/"})
    public Authentication getAuthCurrentUser();
    }
