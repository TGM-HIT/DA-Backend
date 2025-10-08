package at.ac.tgm.webuntis;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bytedream.untis4j.LoginException;
import org.bytedream.untis4j.Session;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;

@Getter
@Component
@SessionScope
@Slf4j
public class WebUntisSession {
    private Session session;
    
    public void login(String adUsername, String password) {
        try {
            
            String webUntisUsername = adUsername != null && adUsername.contains("@")
                    ? adUsername.split("@")[0]
                    : adUsername;
            try {
                session = Session.login(webUntisUsername, password, "https://neilo.webuntis.com", "tgm");
            } catch (LoginException e) {
                log.error(e.getMessage());
                throw new BadCredentialsException("Login to WebUntis failed with your credentials");
            }
        } catch (IOException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }
    
    public void logout() {
        if (session != null) {
            try {
                session.logout();
            } catch (IOException e) {
                throw new IllegalStateException("WebUntis logout failed", e);
            }
        }
    }
}
