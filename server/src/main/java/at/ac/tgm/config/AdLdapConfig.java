package at.ac.tgm.config;

import at.ac.tgm.ad.Roles;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import javax.naming.Name;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableLdapRepositories
public class AdLdapConfig {
    @Value("${spring.ldap.urls}")
    private String url;
    @Value("${spring.ldap.domain}")
    private String domain;
    
    @Bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider(GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        authenticationProvider.setConvertSubErrorCodesToExceptions(true);
        authenticationProvider.setUseAuthenticationRequestCredentials(true);
        authenticationProvider.setSearchFilter("(&(objectClass=user)(sAMAccountName={1}))");
        authenticationProvider.setAuthoritiesMapper(grantedAuthoritiesMapper);
        return authenticationProvider;
    }
    
    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);
            if (mappedAuthorities.stream().anyMatch((authority) -> authority.getAuthority().contains("lehrer"))) {
                mappedAuthorities.add(new SimpleGrantedAuthority(Roles.TEACHER));
            }
            if (mappedAuthorities.stream().anyMatch((authority) -> authority.getAuthority().contains("schueler"))) {
                mappedAuthorities.add(new SimpleGrantedAuthority(Roles.STUDENT));
            }
            return mappedAuthorities;
        };
    }
    
    @Bean
    public AuthenticationManager authenticationManager(ActiveDirectoryLdapAuthenticationProvider adProvider) {
        return new ProviderManager(Collections.singletonList(adProvider));
    }
    
    @Bean
    public ObjectMapper registerObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("MyNameSerializer");
        module.addSerializer(Name.class, new NameJsonSerializer());
        mapper.registerModule(module);
        return mapper;
    }
    
    static class NameJsonSerializer extends StdSerializer<Name> {
        public NameJsonSerializer() {
            this(null);
        }
        
        public NameJsonSerializer(Class<Name> t) {
            super(t);
        }
        
        @Override
        public void serialize(Name value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.toString());
        }
    }
}
