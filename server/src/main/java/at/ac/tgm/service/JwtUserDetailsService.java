package at.ac.tgm.service;

import at.ac.tgm.UserRoles;
import at.ac.tgm.ad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final UserService userService;
    
    @Autowired
    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByUserPrincipalName(username).map((entry) -> new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(UserRoles.getRoleFromDn(entry.getDistinguishedName())));
            }
            
            @Override
            public String getPassword() {
                return null;
            }
            
            @Override
            public String getUsername() {
                return entry.getUserPrincipalName();
            }
        }).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
