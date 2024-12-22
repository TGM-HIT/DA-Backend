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
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
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
    
    /*@Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }*/
    
    @Bean
    public HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }
    
    @Bean
    CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler() {
        return new CsrfTokenRequestAttributeHandler();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            //CookieCsrfTokenRepository cookieCsrfTokenRepository,
            HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository,
            CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler
    ) throws Exception {
        return http
                .csrf((csrf) -> {
                    csrf.csrfTokenRepository(httpSessionCsrfTokenRepository);
                    //csrf.csrfTokenRepository(cookieCsrfTokenRepository);
                    csrf.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler);
                })
                .securityContext((context) -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers("/", "/auth/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
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
                        .exposedHeaders("Access-Control-Allow-Origin", "X-CSRF-TOKEN");
            }
        };
    }
    
}
