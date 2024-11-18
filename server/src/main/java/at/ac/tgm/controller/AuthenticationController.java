package at.ac.tgm.controller;

import at.ac.tgm.dto.AuthenticationRequest;
import at.ac.tgm.dto.AuthenticationResponse;
import at.ac.tgm.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
public class AuthenticationController {
    public static final String EXAMPLE_ACCESS_TOKEN = """
            {"accessToken": "wyJhbGciOiJSXzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtcG9pblRuZXJAdGdtLmFjLmFOIiwicm9sZSI6IkxFSFJFUiIsImlzcyI6IlRHTSIsImlhdCI6MTczMTg6ODU1MCwiZXhwIjoxNzMxODkyMTUwfQ.gstveTZ80osOpOOdFLchdm-DThYwQf2FoGBpThH7Cl0PB3VcjP7XXlaT9cRoeqZ4-QI6x4PRwySx79UHKDRQy4jU2e9LMIXzZLyl6qZFRblmj8R1fpI5dps6RsYPGOojV52ASbrKczP5NRK4rJiXBqEW4pZpN1GnCb2KtCQlbMFBOd7etR8UHTULTATxWP7AY_uTuVG4fPE5XwXdnkTppcBLxZXf-wuHZvB8WQc5J-28kE5KG1ozJubyX7EQAEgb-UW5BAS3q_jHgGaNj0CljjXm2vr2S_T1WwE_d-RSKAAYse_jpKEhqAKfiIIYbjkrhellS4pQwVQ8bRqb4KP2Qr"}""";
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    
    @Autowired
    public AuthenticationController(final AuthenticationManager authenticationManager,
                                    final JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }
    
    @Operation(summary = "Login",
            responses = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(examples = {
                                    @ExampleObject(name = "Beispieltoken", value = EXAMPLE_ACCESS_TOKEN)
                            }, schema = @Schema(implementation = AuthenticationResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(
            @RequestBody @Valid final AuthenticationRequest authenticationRequest) {
        UsernamePasswordAuthenticationToken authenticate;
        try {
            authenticate = (UsernamePasswordAuthenticationToken) authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (final BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
        final AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAccessToken(jwtTokenService.generateToken(authenticate));
        return authenticationResponse;
    }

}
