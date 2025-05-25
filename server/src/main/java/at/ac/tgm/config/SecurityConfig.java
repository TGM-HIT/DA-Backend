package at.ac.tgm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
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
            CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler,
            AccessDeniedHandler accessDeniedHandler,
            AuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        return http
                .csrf((csrf) -> {
                    csrf.ignoringRequestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/auth/login");
                    csrf.csrfTokenRepository(cookieCsrfTokenRepository);
                    csrf.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler);
                    csrf.configure(http); // Wichtig, damit das neue Einstellungen übernommen werden
                })
                .securityContext((context) -> context.securityContextRepository(securityContextRepository))
                .anonymous(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers(
                                        "/",
                                        "/auth/**", // Login-Controller
                                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/swagger-config", // OpenAPI Documentation
                                        "/error", // Fehlerseiten
                                        "/h2-console/**", // H2-Konsole
                                        "/diplomdb/**.html", // Statische Testseiten erlauben
                                        "/logs", "/logs/**", // Temporär für leichteres Debugging
                                        "/**.ico" // Icons erlauben
                                        // Alle weiteren Anfragen erfordern Authentifizierung
                                ).permitAll()
                                .requestMatchers(HttpMethod.OPTIONS).permitAll() // Für Preflight bei unterschiedlichen Ports
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler(accessDeniedHandler);
                    exception.authenticationEntryPoint(authenticationEntryPoint);
                })
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
                        .allowedOriginPatterns("http://localhost", "http://localhost:[*]", "https://projekte.tgm.ac.at", "https://*.projekte.tgm.ac.at")
                        .exposedHeaders("Access-Control-Allow-Origin");
            }
        };
    }
    
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            private static final Logger log = LoggerFactory.getLogger(AccessDeniedHandler.class);
            
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
                log.info("CustomAccessDeniedHandler {}", accessDeniedException.getMessage());
                // Both header are important, else Axios Network error
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "*");
                // TODO Fix this, causes "No converter for [class java.util.LinkedHashMap] with preset Content-Type 'text/plain'"
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.getWriter().write("You don't have the necessary role to access this resource");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        };
    }
    
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            private static final Logger log = LoggerFactory.getLogger(AuthenticationEntryPoint.class);
            
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                log.info("CustomAuthenticationEntryPoint {}", authException.getMessage());
                // Both header are important, else Axios Network error
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "*");
                // TODO Fix this, causes "No converter for [class java.util.LinkedHashMap] with preset Content-Type 'text/plain'"
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.getWriter().write("You must be logged-in to access this resource");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        };
    }
}
