package at.ac.tgm;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
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
    
    @Bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider = new ActiveDirectoryLdapAuthenticationProvider("tgm.ac.at", "ldap://10.2.24.151/");
        authenticationProvider.setConvertSubErrorCodesToExceptions(true);
        authenticationProvider.setUseAuthenticationRequestCredentials(true);
        return authenticationProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(ActiveDirectoryLdapAuthenticationProvider adProvider) {
        return new ProviderManager(Collections.singletonList(adProvider));
    }
    
    @Bean
    public ObjectMapper registerObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("MyNameSerializer");
        module.addSerializer(Name.class, new NameJsonSerializer());
        mapper.registerModule(module);
        return mapper;
    }
    
    class NameJsonSerializer extends StdSerializer<Name> {
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
