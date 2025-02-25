package at.ac.tgm.diplomarbeit.diplomdb.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurationsklasse für Swagger/OpenAPI.
 * Diese Klasse definiert die Dokumentationseinstellungen für die Diplomarbeit REST API,
 * einschließlich Titel, Beschreibung, Version und einem Verweis auf weitere Informationen.
 */
//@Configuration
public class SwaggerConfig {

    /**
     * Erstellt und konfiguriert die OpenAPI-Dokumentation.
     *
     * @return das OpenAPI-Objekt mit den definierten API-Informationen und externen Dokumentationsdetails
     */
    @Bean
    public OpenAPI diplomarbeitOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Diplomarbeit API - Luan Sherifi")
                        .description("Dokumentation der Diplomarbeit REST API")
                        .version("v1.2"))
                .externalDocs(new ExternalDocumentation()
                        .description("Weitere Informationen")
                        .url("https://www.example.com"));
    }
}
