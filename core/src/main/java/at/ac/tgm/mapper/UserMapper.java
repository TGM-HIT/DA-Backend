package at.ac.tgm.mapper;

import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.dto.LoginTypeEnum;
import at.ac.tgm.dto.UserDto;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class UserMapper {
    public UserDto authenticationToUserDto(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oAuth2) {
            return oAuth2AuthenticationTokenToUserDto(oAuth2);
        }
        if (auth instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
            return usernamePasswordAuthenticationTokenToUserDto(usernamePasswordAuthenticationToken);
        }
        if (auth instanceof TestingAuthenticationToken testingAuthenticationToken) {
            return testingAuthenticationTokenToUserDto(testingAuthenticationToken);
        }
        throw new IllegalStateException("Unknown Authentication object");
    }
    
    public String getUsername(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oAuth2) {
            return getUsername(oAuth2);
        }
        if (auth instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
            return getUsername(usernamePasswordAuthenticationToken);
        }
        if (auth instanceof TestingAuthenticationToken testingAuthenticationToken) {
            return getUsername(testingAuthenticationToken);
        }
        throw new IllegalStateException("Unknown Authentication object");
    }
    
    public UserDto oAuth2AuthenticationTokenToUserDto(OAuth2AuthenticationToken token) {
        if (token.getPrincipal() == null || !(token.getPrincipal() instanceof DefaultOidcUser principal)) {
            throw new IllegalStateException("principal is null or not a DefaultOidcUser");
        }
        String username = principal.getAttribute("preferred_username");
        String fullname = capitalizeName(principal.getAttribute("name"));
        List<String> roles = getRoles(token.getAuthorities());
        return  new UserDto(username, fullname, roles, LoginTypeEnum.OIDC);
    }
    
    public String getUsername(OAuth2AuthenticationToken token) {
        if (token.getPrincipal() == null || !(token.getPrincipal() instanceof DefaultOidcUser principal)) {
            throw new IllegalStateException("principal is null or not a DefaultOidcUser");
        }
        return principal.getAttribute("preferred_username");
    }
    
    public UserDto usernamePasswordAuthenticationTokenToUserDto(UsernamePasswordAuthenticationToken token) {
        if (token.getPrincipal() == null || !(token.getPrincipal() instanceof LdapUserDetailsImpl principal)) {
            throw new IllegalStateException("principal is null or not a LdapUserDetailsImpl");
        }
        String username = principal.getUsername();
        if (token.getDetails() == null || !(token.getDetails() instanceof UserEntry userEntry)) {
            throw new IllegalStateException("userEntry is null or not a UserEntry");
        }
        String fullname = userEntry.getCn();
        List<String> roles = getRoles(token.getAuthorities());
        return new UserDto(username, fullname, roles, LoginTypeEnum.LDAP);
    }
    
    public String getUsername(UsernamePasswordAuthenticationToken token) {
        if (token.getPrincipal() == null || !(token.getPrincipal() instanceof LdapUserDetailsImpl principal)) {
            throw new IllegalStateException("principal is null or not a LdapUserDetailsImpl");
        }
        return principal.getUsername();
    }
    
    public UserDto testingAuthenticationTokenToUserDto(TestingAuthenticationToken token) {
        String username = (String) token.getPrincipal();
        if (token.getDetails() == null || !(token.getDetails() instanceof UserEntry userEntry)) {
            throw new IllegalStateException("userEntry is null or not a UserEntry");
        }
        String fullname = userEntry.getCn();
        List<String> roles = getRoles(token.getAuthorities());
        return new UserDto(username, fullname, roles, LoginTypeEnum.TESTING);
    }
    
    public String getUsername(TestingAuthenticationToken token) {
        return (String) token.getPrincipal();
    }
    
    private static List<String> getRoles(Collection<GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .toList();
    }
    
    private static String capitalizeName(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder(input.length());
        boolean capitalizeNext = true;
        
        for (char c : input.toLowerCase().toCharArray()) {
            if (capitalizeNext && Character.isLetter(c)) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
            
            if (c == ' ' || c == '-') {
                capitalizeNext = true;
            }
        }
        
        return result.toString();
    }
}
