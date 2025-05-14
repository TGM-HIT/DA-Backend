package at.ac.tgm.controller;

import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.ad.util.Util;
import at.ac.tgm.api.AuthenticationApi;
import at.ac.tgm.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AuthenticationController implements AuthenticationApi {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserDetailsContextMapper userDetailsContextMapper;
    
    @Autowired
    private Environment env;
    
    @Autowired
    private SecurityContextRepository securityContextRepository;
    
    @Autowired
    private UserService userService;
    
    @Override
    public ResponseEntity<Authentication> authenticateUser(LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
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
        session.invalidate();
        logger.info("Session invalidated, User logged out successfully");
        return ResponseEntity.ok("User logged out successfully");
    }

    @Override
    public ResponseEntity<Authentication> getAuthCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } else {
            return ResponseEntity.ok(auth);
        }
    }
}
