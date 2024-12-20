package at.ac.tgm.config;

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
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import javax.naming.Name;
import java.io.IOException;
import java.util.Collections;

@Configuration
@EnableLdapRepositories
public class AdLdapConfig {
    @Value("${spring.ldap.urls}")
    private String url;
    @Value("${spring.ldap.domain}")
    private String domain;
    
    @Bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        authenticationProvider.setConvertSubErrorCodesToExceptions(true);
        authenticationProvider.setUseAuthenticationRequestCredentials(true);
        return authenticationProvider;
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
