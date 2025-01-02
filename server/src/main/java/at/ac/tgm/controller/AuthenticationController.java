package at.ac.tgm.controller;

import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.ad.util.Util;
import at.ac.tgm.dto.LoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;
    
    @Autowired
    private Environment env;
    
    @Autowired
    private SecurityContextRepository securityContextRepository;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
                    @Content(examples = {@ExampleObject(name = "Simulate Teacher Login", value = """
                                    {"username":"mpointner@tgm.ac.at", "password":"", "simulate":true}""")
                    }, schema = @Schema(implementation = LoginRequestDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
            }))
    public ResponseEntity<Authentication> authenticateUser(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        UserEntry user = (loginRequest.getUsername().contains("@")
                ? userService.findByMail(loginRequest.getUsername())
                : userService.findBysAMAccountName(loginRequest.getUsername()))
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden"));
        
        Authentication authentication = getAuthentication(loginRequest, user);
        
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);
        
        logger.info("Login of " + user.getDisplayName());
        
        return ResponseEntity.ok(authentication);
    }
    
    private Authentication getAuthentication(LoginRequestDto loginRequest, UserEntry user) {
        if (loginRequest.getSimulate() != null && loginRequest.getSimulate()) {
            if (!env.matchesProfiles("dev")) {
                throw new IllegalArgumentException("Simulate just allowed with profile dev");
            }
            List<SimpleGrantedAuthority> authorities = user.getMemberOf().stream().map((memberOf) -> new SimpleGrantedAuthority(Util.getCnFromName(memberOf))).toList();
            TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(user.getMail(), null, grantedAuthoritiesMapper.mapAuthorities(authorities));
            authenticationToken.setDetails(user);
            return authenticationToken;
        } else {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getMail(), loginRequest.getPassword());
            authenticationToken.setDetails(user);
            return authenticationManager.authenticate(authenticationToken);
        }
    }
    
    
    @GetMapping("/csrf-token")
    @Operation(summary = "The CSRF-Token is returned on any call as cookie but if you want to get it explicitly in the body, you can do so with this endpoint")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
    
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        logger.info("Session invalidated, User logged out successfully");
        return ResponseEntity.ok("User logged out successfully");
    }
}
