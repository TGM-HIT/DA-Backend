package at.ac.tgm.config;

import at.ac.tgm.ad.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.*;

@Configuration
@EnableLdapRepositories
public class AdLdapConfig {

    @Autowired
    private AdminConfigLoader adminConfigLoader;

    @Bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider(UserDetailsContextMapper userDetailsContextMapper) {
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider =
                new ActiveDirectoryLdapAuthenticationProvider("tgm.ac.at", "ldap://10.2.24.151/");
        authenticationProvider.setConvertSubErrorCodesToExceptions(true);
        authenticationProvider.setUseAuthenticationRequestCredentials(true);
        // Filtern nach sAMAccountName
        authenticationProvider.setSearchFilter("(&(objectClass=user)(sAMAccountName={1}))");
        authenticationProvider.setUserDetailsContextMapper(userDetailsContextMapper);
        return authenticationProvider;
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new LdapUserDetailsMapper(){
            @Override
            protected Collection<org.springframework.security.core.GrantedAuthority> addCustomAuthorities(
                    org.springframework.security.core.userdetails.UserDetails user,
                    Collection<org.springframework.security.core.GrantedAuthority> authorities) {

                // Falls dein AD-Login-Name = "mtomi"
                String username = user.getUsername(); // z. B. "mtomi"

                // Prüfen, ob in admins.json
                if (adminConfigLoader.getAdmins().contains(username)) {
                    List<org.springframework.security.core.GrantedAuthority> extended = new ArrayList<>(authorities);
                    extended.add(new SimpleGrantedAuthority(Roles.ADMIN));
                    return extended;
                }
                return authorities;
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(ActiveDirectoryLdapAuthenticationProvider adProvider) {
        return new ProviderManager(Collections.singletonList(adProvider));
    }
}
