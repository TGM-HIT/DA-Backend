package at.ac.tgm.config;

import at.ac.tgm.ad.Roles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.*;

@Configuration
@EnableLdapRepositories(basePackages = "at.ac.tgm.ad.repository")
public class AdLdapConfig {
    @Value("${spring.ldap.urls}")
    private String url;
    @Value("${spring.ldap.domain}")
    private String domain;
    @Value("${admins}")
    private List<String> admins;
    
    @Bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider(UserDetailsContextMapper userDetailsContextMapper) {
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        authenticationProvider.setConvertSubErrorCodesToExceptions(true);
        authenticationProvider.setUseAuthenticationRequestCredentials(true);
        authenticationProvider.setSearchFilter("(&(objectClass=user)(sAMAccountName={1}))");
        authenticationProvider.setUserDetailsContextMapper(userDetailsContextMapper);
        return authenticationProvider;
    }
    
    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new LdapUserDetailsMapper(){
            @Override
            public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
                Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);
                if (admins != null && admins.contains(username)) {
                    mappedAuthorities.add(new SimpleGrantedAuthority(Roles.ADMIN));
                }
                if (authorities.stream().anyMatch((authority) -> authority.getAuthority().contains("lehrer"))) {
                    mappedAuthorities.add(new SimpleGrantedAuthority(Roles.TEACHER));
                }
                if (authorities.stream().anyMatch((authority) -> authority.getAuthority().contains("schueler"))) {
                    mappedAuthorities.add(new SimpleGrantedAuthority(Roles.STUDENT));
                }
                return super.mapUserFromContext(ctx, username, mappedAuthorities);
            }
        };
    }
    
    @Bean
    public AuthenticationManager authenticationManager(ActiveDirectoryLdapAuthenticationProvider adProvider) {
        return new ProviderManager(Collections.singletonList(adProvider));
    }
}
