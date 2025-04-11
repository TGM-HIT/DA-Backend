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
    private String webunitsUser;
    @Value("${webuntis.password}")
    private String webunitsPassword;
    @Value("${spring.ldap.username}")
    private String activeDirectoryUser;
    @Value("${spring.ldap.password}")
    private String activeDirectoryPassword;
    
    @Bean
    public Session login() throws IOException {
        String activeDirectoryUsername = activeDirectoryUser != null && activeDirectoryUser.contains("@")
                ? activeDirectoryUser.split("@")[0]
                : activeDirectoryUser;
        String user = webunitsUser != null && !webunitsUser.isBlank()
                ? webunitsUser
                : activeDirectoryUsername;
        String password = webunitsPassword != null && !webunitsPassword.isBlank()
                ? webunitsPassword
                : activeDirectoryPassword;
        Session session;
        try {
            session = Session.login(user, password, "https://neilo.webuntis.com", "tgm");
        } catch (LoginException e) {
            log.error(e.getMessage());
            throw new BadCredentialsException("Login to WebUntis failed with your credentials");
        }
        return session;
    }
}
