package at.ac.tgm.controller;

import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.ad.util.Util;
import at.ac.tgm.api.AuthenticationApi;
import at.ac.tgm.dto.LoginRequestDto;
import at.ac.tgm.dto.UserDto;
import at.ac.tgm.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class AuthenticationController implements AuthenticationApi {
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
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public ResponseEntity<UserDto> login(LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        UserEntry user = (loginRequest.getUsername().contains("@")
                ? userService.findByMail(loginRequest.getUsername())
                : userService.findBysAMAccountName(loginRequest.getUsername()))
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        
        Authentication authentication = getAuthentication(loginRequest, user);
        
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);
        
        log.info("Login of {}", user.getDisplayName());
        
        return ResponseEntity.ok(userMapper.authenticationToUserDto(authentication));
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

    /*@Override
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }*/
    
    @Override
    public ResponseEntity<UserDto> getAuthCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.ok(userMapper.authenticationToUserDto(auth));
        }
    }
    
    @Override
    public ResponseEntity<Boolean> isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(auth != null);
    }
}
