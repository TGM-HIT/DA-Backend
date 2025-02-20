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
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import javax.naming.Name;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Configuration
@EnableLdapRepositories
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
    
    @Bean
    public ObjectMapper registerObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("MyObjectSerializer");
        module.addSerializer(Name.class, new NameJsonSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
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
    
    static class LocalDateTimeJsonSerializer extends StdSerializer<LocalDateTime> {
        public LocalDateTimeJsonSerializer() {
            this(null);
        }
        
        public LocalDateTimeJsonSerializer(Class<LocalDateTime> t) {
            super(t);
        }
        
        @Override
        public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }
}
