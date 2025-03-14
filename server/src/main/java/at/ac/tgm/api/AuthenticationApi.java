package at.ac.tgm.api;

import at.ac.tgm.dto.LoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "Login a user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
                    @Content(examples = {
                            @ExampleObject(name = "Real Login", value = """
                                    {"username":"REPLACE_WITH_YOUR_TGM_USERNAME", "password":"REPLACE_WITH_YOUR_TGM_PASSWORD"}"""),
                            @ExampleObject(name = "Simulate Teacher Login", value = """
                                    {"username":"mpointner", "password":"", "simulate":true}""",
                                    description = """
                                            User simulation is just possible if the application is started with active profile "dev" set. To set the profile, go to Run Configurations -> Edit Configurations -> Active Profiles""")
                    }, schema = @Schema(implementation = LoginRequestDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
            }))
    ResponseEntity<Authentication> login(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response);

    @GetMapping("/csrf-token")
    @Operation(summary = "The CSRF-Token is returned on any call as cookie but if you want to get it explicitly in the body, you can do so with this endpoint")
    CsrfToken csrfToken(HttpServletRequest request);

    @PostMapping("/logout")
    @Operation(summary = "Logout the current logged-in user", description = "Does logout the current logged-in user by invalidating the current session.")
    ResponseEntity<String> logout(HttpSession session);
    
    @GetMapping({""})
    @Operation(summary = "Check if the user is logged in",
            description = "Return 200 with the Authentication Object if the user is logged-in, else 401.",
            responses = {
                    @ApiResponse(description = "Logged-in", responseCode = "200", content = {@Content(schema = @Schema(implementation = Authentication.class))})
            })
    ResponseEntity<Authentication> getAuthCurrentUser();
}
