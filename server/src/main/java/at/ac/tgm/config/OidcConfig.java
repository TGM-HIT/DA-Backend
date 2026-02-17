package at.ac.tgm.config;

import at.ac.tgm.ad.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class OidcConfig {
    /*
    @Bean
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService();
    }
    
    public static class CustomOidcUserService extends OidcUserService {
        @Override
        public OidcUser loadUser(OidcUserRequest userRequest) {
            OidcUser oidcUser = super.loadUser(userRequest);
            
            // Optionally enrich user with custom authorities or session data
            // For example:
            // Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
            // attributes.put("customAttribute", "value");
            
            return oidcUser;
        }
    }*/
    
    @Bean
    GrantedAuthoritiesMapper authenticationConverter() {
        return sourceAuthorities -> sourceAuthorities.stream()
                .filter(OidcUserAuthority.class::isInstance)
                .map(OidcUserAuthority.class::cast)
                .map(OidcUserAuthority::getIdToken)
                .map(OidcIdToken::getClaims)
                .map(this::convertRealmRolesToAuthorities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
    
    private Collection<GrantedAuthority> convertRealmRolesToAuthorities(Map<String, Object> claims) {
        if (claims == null) {
            return List.of();
        }
        
        var realmAccessObj = claims.get("realm_access");
        if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }
        
        var rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> roleList)) {
            return List.of();
        }
        
        return roleList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(Roles::contains)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}

