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
                        .allowedOriginPatterns("http://localhost", "http://localhost:[*]", "https://projekte.tgm.ac.at", "https://*.projekte.tgm.ac.at")
                        .exposedHeaders("Access-Control-Allow-Origin");
            }
            
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                // Only apply /api to @RestController classes
                //configurer.addPathPrefix("/api", clazz -> clazz.isAnnotationPresent(RestController.class));
            }
        };
    }
}
