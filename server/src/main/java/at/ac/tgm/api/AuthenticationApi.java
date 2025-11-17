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
    
    /*@GetMapping("/csrf-token")
    @Operation(summary = "The CSRF-Token is returned on any call as cookie but if you want to get it explicitly in the body, you can do so with this endpoint",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(examples = {
                            @ExampleObject(value = """
                                    {
                                      "parameterName": "_csrf",
                                      "token": "e36a898b-f11e-4605-abf4-9d7ff1900ba1",
                                      "headerName": "X-XSRF-TOKEN"
                                    }
                                    """)}, schema = @Schema(implementation = CsrfToken.class), mediaType = MediaType.APPLICATION_JSON_VALUE))}
    )
    CsrfToken csrfToken(HttpServletRequest request);*/
    
    @PostMapping("/logout")
    @Operation(summary = "Logout the current logged-in user", description = "Does logout the current logged-in user by invalidating the current session.",
            responses = {
                    @ApiResponse(responseCode = "200", content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Log out successful", value = "User logged out successfully"),
                                    @ExampleObject(name = "Already logged-out", value = "User already logged-out or was never logged-in")
                            }, schema = @Schema(implementation = String.class), mediaType = MediaType.TEXT_PLAIN_VALUE)})
            })
    ResponseEntity<String> logout(HttpSession session);
    
    @GetMapping({""})
    @Operation(summary = "Get the current logged-in user",
            description = "Return 200 with the Authentication Object if the user is logged-in, else 204.",
            responses = {
                    @ApiResponse(description = "Logged-in", responseCode = "200", content = {
                            @Content(examples = @ExampleObject(value = """
                                    {
                                      "authorities": [
                                        {
                                          "authority": "lehrer1AHIT"
                                        },
                                        {
                                          "authority": "alle"
                                        },
                                        {
                                          "authority": "lehrerHIT"
                                        },
                                        {
                                          "authority": "everyonePassword"
                                        },
                                        {
                                          "authority": "lehrerORD"
                                        },
                                        {
                                          "authority": "ROLE_TEACHER"
                                        }
                                      ],
                                      "details": {
                                        "id": "CN=Michael Pointner,OU=Lehrer,OU=People",
                                        "memberOf": [
                                          "CN=lehrerORD,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at",
                                          "CN=alle,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at",
                                          "CN=lehrer1AHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at",
                                          "CN=lehrerHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at",
                                          "CN=everyonePassword,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at"
                                        ],
                                        "cn": "Michael Pointner",
                                        "sn": "Pointner",
                                        "displayName": "Pointner Michael",
                                        "distinguishedName": "CN=Michael Pointner,OU=Lehrer,OU=People,OU=tgm,DC=tgm,DC=ac,DC=at",
                                        "employeeNumber": "POIM",
                                        "employeeType": "lehrer",
                                        "givenName": "Michael",
                                        "info": "Sprechstunde: Mittwoch 15:10-16:00\\r\\nRaum: H129A ",
                                        "mail": "mpointner@tgm.ac.at",
                                        "msExchUserCulture": "de-AT",
                                        "name": "Michael Pointner",
                                        "objectCategory": "CN=Person,CN=Schema,CN=Configuration,DC=tgm,DC=ac,DC=at",
                                        "objectClass": [
                                          "top",
                                          "person",
                                          "organizationalPerson",
                                          "user"
                                        ],
                                        "userPrincipalName": "mpointner@tgm.ac.at"
                                      },
                                      "authenticated": true,
                                      "credentials": null,
                                      "principal": "mpointner",
                                      "name": "mpointner"
                                    }
                                    """), schema = @Schema(implementation = Authentication.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                    @ApiResponse(description = "No logged-in", responseCode = "204")
            })
    ResponseEntity<Authentication> getAuthCurrentUser();
    
    @GetMapping({"/status"})
    @Operation(summary = "Get if user is logged-in",
            description = "Return 200 with the log-in status",
            responses = {
                    @ApiResponse(description = "Logged-in status", responseCode = "200", content = {@Content(examples = {
                            @ExampleObject(name = "Logged-in", value = "true"),
                            @ExampleObject(name = "Logged-out", value = "false")
                    }, schema = @Schema(implementation = Boolean.class), mediaType = MediaType.TEXT_PLAIN_VALUE)})
            })
    ResponseEntity<Boolean> isLoggedIn();
    
    
}
