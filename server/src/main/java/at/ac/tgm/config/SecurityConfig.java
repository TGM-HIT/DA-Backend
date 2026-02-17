package at.ac.tgm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    @Value("${frontend-uri}")
    private String frontendUri;
    
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    
    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieCustomizer(builder -> {
            builder
                    .sameSite("None")
                    .secure(true)
                    .httpOnly(false);
        });
        return repo;
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
            AuthenticationEntryPoint authenticationEntryPoint,
            ClientRegistrationRepository clientRegistrationRepository/*,
            OidcConfig.CustomOidcUserService oidcUserService*/
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
                /*.csrf((csrf) -> {
                    csrf.ignoringRequestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/auth/**", "/h2-console/**");
                    csrf.csrfTokenRepository(cookieCsrfTokenRepository);
                    csrf.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler);
                    csrf.configure(http); // Wichtig, damit das neue Einstellungen übernommen werden
                })*/
        http.securityContext((context) -> context.securityContextRepository(securityContextRepository));
        http.anonymous(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests((authorize) ->
                authorize
                        .requestMatchers(
                                "/",
                                "/auth/**", // Login-Controller
                                "/oauth2/**",             // Spring's OAuth2 authorization endpoints
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
        );
        http.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)); // h2 Console
        http.exceptionHandling(exception -> {
            exception.accessDeniedHandler(accessDeniedHandler);
            exception.authenticationEntryPoint(authenticationEntryPoint);
        });
        
        http.oauth2Login(oauth2 -> oauth2
                //.userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                .defaultSuccessUrl(frontendUri, true)
        );
        http.oauth2Client(Customizer.withDefaults());
        
        http.logout(logout -> {
            logout.logoutUrl("/auth/logout");
            logout.logoutSuccessHandler((request, response, authentication) -> {
                String referer = request.getHeader("Referer");
                if (authentication instanceof OAuth2AuthenticationToken) {
                    var logoutHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
                    logoutHandler.setPostLogoutRedirectUri(referer);
                    logoutHandler.onLogoutSuccess(request, response, authentication);
                } else {
                    response.sendRedirect(referer);
                }
            });
            logout.invalidateHttpSession(true);
            logout.deleteCookies("JSESSIONID");
        });
        
        return http.build();
    }
    
    /*@Bean
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
    }*/
    
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
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                // Don't use sendError here, else No converter for [class java.util.LinkedHashMap] with preset Content-Type 'text/plain' error
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("You don't have the necessary role to access this resource");
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
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                // Don't use sendError here, else No converter for [class java.util.LinkedHashMap] with preset Content-Type 'text/plain' error
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("You must be logged-in to access this resource");
            }
        };
    }
}
