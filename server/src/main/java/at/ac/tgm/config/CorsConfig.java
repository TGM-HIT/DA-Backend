package at.ac.tgm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedHeaders("*")
                        .allowedMethods("*")
                        .allowCredentials(true)
                        .allowedOriginPatterns(
                                "http://localhost:8080", // When Frontend is served from static/ folder
                                "http://localhost:8081", // Alternative
                                "http://localhost:8082", // Alternative
                                "http://localhost:5173", // Vite dev port
                                "http://localhost:5174", // Alternative
                                "http://localhost:5175", // Alternative
                                "https://*.projekte.tgm.ac.at"
                        );
            }
            
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                // Only apply /api to @RestController classes
                //configurer.addPathPrefix("/api", clazz -> clazz.isAnnotationPresent(RestController.class));
            }
        };
    }
}
