package at.ac.tgm.config;

import lombok.extern.slf4j.Slf4j;
import org.bytedream.untis4j.LoginException;
import org.bytedream.untis4j.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

@Slf4j
@Configuration
public class WebUntisConfig {
    @Value("${webuntis.username}")
    private String WEBUNITS_USER;
    @Value("${webuntis.password}")
    private String WEBUNITS_PASSWORD;
    @Value("${spring.ldap.username}")
    private String AD_USER;
    @Value("${spring.ldap.password}")
    private String AD_PASSWORD;
    
    @Bean
    public Session login() {
        String user = WEBUNITS_USER != null && !WEBUNITS_USER.isBlank()
                ? WEBUNITS_USER
                : (AD_USER.contains("@") ? AD_USER.split("@")[0] : AD_USER);
        String password = WEBUNITS_PASSWORD != null && !WEBUNITS_PASSWORD.isBlank()
                ? WEBUNITS_PASSWORD
                : AD_PASSWORD;
        Session session;
        try {
            session = Session.login(user, password, "https://neilo.webuntis.com", "tgm");
        } catch (LoginException e) {
            log.error(e.getMessage());
            throw new BadCredentialsException("Login to WebUntis failed with your credentials");
        } catch (IOException e) {
            log.error(e.getMessage(), e.getCause());
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return session;
    }
}
