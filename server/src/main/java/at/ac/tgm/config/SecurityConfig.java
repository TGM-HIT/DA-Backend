package at.ac.tgm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    
    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        return cookieCsrfTokenRepository;
    }
    
    @Bean
    public CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler() {
        return new CsrfTokenRequestAttributeHandler();
    }
    
    /*
    If you enable those defaults, CSRF-Token are automatically attached in the header:
    axios.defaults.withCredentials = true
    axios.defaults.withXSRFToken = true
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            CookieCsrfTokenRepository cookieCsrfTokenRepository,
            CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler
    ) throws Exception {
        return http
                .csrf((csrf) -> {
                    csrf.csrfTokenRepository(cookieCsrfTokenRepository);
                    csrf.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler);
                    csrf.configure(http); // Wichtig, damit das neue Einstellungen übernommen werden
                })
                .securityContext((context) -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers("/", "/auth/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS).permitAll() // Für Preflight bei unterschiedlichen Ports
                                .anyRequest().authenticated()
                )
                .build();
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedHeaders("*")
                        .allowedMethods("*")
                        .allowCredentials(true)
                        .allowedOriginPatterns("http://localhost:[*]", "https://projekte.tgm.ac.at")
                        .exposedHeaders("Access-Control-Allow-Origin");
            }
        };
    }
    
}
