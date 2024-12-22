package at.ac.tgm.controller;

import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.ad.util.Util;
import at.ac.tgm.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private SecurityContextRepository securityContextRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * This method is for getting the CSRF-Token in case of Session-Storage.
     *
     * Usage:
     * let csrf: string | null = null;
     *
     * function csrfToken() {
     *   axios.get(domain + "/auth/csrf-token").then((response) => {
     *     csrf = response.data.token
     *   });
     * }
     *
     * axios.interceptors.request.use((request) => {
     *   if (!request.headers.has("X-CSRF-TOKEN")) {
     *     request.headers.set("X-CSRF-TOKEN", csrf)
     *   }
     *   return request;
     * }
     */
    @GetMapping("/csrf-token")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        UserEntry user = (loginRequest.getUsername().contains("@")
                ? userService.findByMail(loginRequest.getUsername())
                : userService.findBysAMAccountName(loginRequest.getUsername()))
                                                   .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden"));
        
        Authentication authentication = null;
        if (loginRequest.getSimulate() == null || !loginRequest.getSimulate()) {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getMail(),
                            loginRequest.getPassword()
                    )
            );
        } else {
            List<SimpleGrantedAuthority> authorities = user.getMemberOf().stream().map((memberOf) -> new SimpleGrantedAuthority(Util.getCnFromName(memberOf))).toList();
            
            authentication = new TestingAuthenticationToken(user.getMail(), null, authorities);
        }
        
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);
        
        
        Optional<UserEntry> userEntryOptional = userService.findByMail(authentication.getName());
        if (userEntryOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found in LDAP");
        }
        
        UserEntry userEntry = userEntryOptional.get();
        String userType = userEntry.getMail().contains("student") ? "student" : "teacher";
        
        return ResponseEntity.ok(Map.of("userType", userType));
    }
    
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        System.out.println("Session invalidated, User logged out successfully");
        return ResponseEntity.ok("User logged out successfully");
    }
}
