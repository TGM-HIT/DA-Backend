package at.ac.tgm.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
            CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler,
            AccessDeniedHandler accessDeniedHandler,
            AuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        return http
                .csrf((csrf) -> {
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
                                                // Öffentliche Endpunkte: Fehlerseiten, H2-Konsole und API-Dokumentation
                                                "/error",
                                                "/h2-console/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/v3/api-docs/**",
                                                // Statische Testseiten erlauben
                                                "/index.html",
                                                "/login_test.html",
                                                "/betreuer_test.html",
                                                "/projects_test.html",
                                                "/documents_test.html",
                                                "/milestones_test.html",
                                                "/project_applications_test.html",
                                                "/schueler_test.html",
                                                "/user_test.html",
                                                // API-Endpunkte für Login, Logout und CSRF-Token abrufen erlauben
                                                "/api/login",
                                                "/api/logout",
                                                "/api/csrf-token"
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
                        .allowedOriginPatterns("http://localhost:[*]", "https://projekte.tgm.ac.at")
                        .exposedHeaders("Access-Control-Allow-Origin");
            }
        };
    }
    
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            private static final Logger logger = LoggerFactory.getLogger(AccessDeniedHandler.class);
            
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                logger.info("CustomAccessDeniedHandler", accessDeniedException.getMessage());
                // Both header are important, else Axios Network error
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setContentType("text/plain");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have the necessary role to access this resource");
            }
        };
    }
    
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            private static final Logger logger = LoggerFactory.getLogger(AuthenticationEntryPoint.class);
            
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                logger.info("CustomAuthenticationEntryPoint", authException.getMessage());
                // Both header are important, else Axios Network error
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setContentType("text/plain");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged-in to access this resource");
            }
        };
    }
    
}
