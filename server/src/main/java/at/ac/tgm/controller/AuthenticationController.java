package at.ac.tgm.controller;

import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.ad.util.Util;
import at.ac.tgm.api.AuthenticationApi;
import at.ac.tgm.dto.LoginRequestDto;
import at.ac.tgm.webuntis.WebUntisSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class AuthenticationController implements AuthenticationApi {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsContextMapper userDetailsContextMapper;
    private final Environment env;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;
    private final WebUntisSession webUntisSession;
    
    public AuthenticationController(
            AuthenticationManager authenticationManager,
            UserDetailsContextMapper userDetailsContextMapper,
            Environment env,
            SecurityContextRepository securityContextRepository,
            UserService userService,
            WebUntisSession webUntisSession
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsContextMapper = userDetailsContextMapper;
        this.env = env;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
        this.webUntisSession = webUntisSession;
    }
    
    @Override
    public ResponseEntity<Authentication> login(LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        UserEntry user = (loginRequest.getUsername().contains("@")
                ? userService.findByMail(loginRequest.getUsername())
                : userService.findBysAMAccountName(loginRequest.getUsername()))
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden"));
        
        Authentication authentication = getAuthentication(loginRequest, user);
        
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);
        
        log.info("Login of {}", user.getDisplayName());
        
        if (loginRequest.getWebuntis() != null && loginRequest.getWebuntis()) {
            webUntisSession.login(loginRequest.getUsername(), loginRequest.getPassword());
        }
        
        return ResponseEntity.ok(authentication);
    }
    
    private Authentication getAuthentication(LoginRequestDto loginRequest, UserEntry user) {
        if (loginRequest.getSimulate() != null && loginRequest.getSimulate()) {
            if (!env.matchesProfiles("dev")) {
                throw new IllegalArgumentException("Simulate just allowed with profile dev");
            }
            List<SimpleGrantedAuthority> authoritiesWithGroups = user.getMemberOf().stream().map((memberOf) -> new SimpleGrantedAuthority(Util.getCnFromName(memberOf))).toList();
            
            // Add Admin, Teacher and Student Role if applicable
            UserDetails userDetails = userDetailsContextMapper.mapUserFromContext(new DirContextAdapter(user.getId()), user.getSAMAccountName(), authoritiesWithGroups);
            
            TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(user.getSAMAccountName(), null, userDetails.getAuthorities());
            authenticationToken.setDetails(user);
            return authenticationToken;
        } else {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getSAMAccountName(), loginRequest.getPassword());
            authenticationToken.setDetails(user);
            return authenticationManager.authenticate(authenticationToken);
        }
    }
    
    @Override
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
    
    @Override
    public ResponseEntity<String> logout(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            session.invalidate();
            log.info("Session invalidated, User logged out successfully");
            return ResponseEntity.ok("User logged out successfully");
        } else {
            return ResponseEntity.ok("User already logged-out or was never logged-in");
        }
    }
    
    @Override
    public ResponseEntity<Authentication> getAuthCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.ok(auth);
        }
    }
    
    @Override
    public ResponseEntity<Boolean> isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(auth != null);
    }
}
