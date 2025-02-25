package at.ac.tgm.diplomarbeit.diplomdb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Sicherheitskonfiguration der Anwendung.
 * Diese Klasse konfiguriert die HTTP-Sicherheit, einschließlich Session-Management, CSRF-Schutz,
 * CORS-Einstellungen und Autorisierungsregeln für öffentliche sowie gesicherte Endpunkte.
 */
//@Configuration
//@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Konfiguriert die SecurityFilterChain für HTTP-Anfragen.
     *
     * @param http das HttpSecurity-Objekt zur Konfiguration
     * @return die konfigurierte SecurityFilterChain
     * @throws Exception falls während der Konfiguration ein Fehler auftritt
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Konfiguration des Security-Kontexts, der das explizite Speichern nicht erfordert
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(false)
                        .securityContextRepository(new HttpSessionSecurityContextRepository())
                )
                // Session-Management mit erforderlicher Session-Erstellung und Schutz vor Session-Fixation
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                // Aktivierung von CORS mit Standardeinstellungen
                .cors(Customizer.withDefaults())
                // CSRF-Schutz konfigurieren und spezifische Endpunkte ausnehmen
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/login"),
                                new AntPathRequestMatcher("/h2-console/**")
                        )
                        .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                )
                // Erlaubt das Einbetten von Inhalten aus derselben Quelle (wichtig für H2-Konsole)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                // Konfiguration der Autorisierungsregeln
                .authorizeHttpRequests(authorize -> authorize
                        // Öffentliche Endpunkte: Fehlerseiten, H2-Konsole und API-Dokumentation
                        .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                        // Statische Testseiten erlauben
                        .requestMatchers(new AntPathRequestMatcher("/index.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login_test.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/betreuer_test.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/projects_test.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/documents_test.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/milestones_test.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/project_applications_test.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/schueler_test.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/user_test.html")).permitAll()
                        // API-Endpunkte für Login, Logout und CSRF-Token abrufen erlauben
                        .requestMatchers(new AntPathRequestMatcher("/api/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/logout")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/csrf-token")).permitAll()
                        // Alle weiteren Anfragen erfordern Authentifizierung
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Konfiguriert den CorsFilter, der alle Ursprünge, Header und Methoden erlaubt.
     *
     * @return der konfigurierte CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // Erlaubt die Übermittlung von Cookies/Authentifizierungsinformationen
        corsConfiguration.setAllowCredentials(true);
        // Erlaubt alle Ursprünge
        corsConfiguration.addAllowedOriginPattern("*");
        // Erlaubt alle Header
        corsConfiguration.addAllowedHeader("*");
        // Erlaubt alle HTTP-Methoden
        corsConfiguration.addAllowedMethod("*");
        // Gibt den CSRF-Token-Header frei
        corsConfiguration.addExposedHeader("X-CSRF-TOKEN");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Registriert die CORS-Konfiguration für alle Pfade
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
