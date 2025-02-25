package at.ac.tgm.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Erstellt und konfiguriert die OpenAPI-Dokumentation.
     *
     * @return das OpenAPI-Objekt mit den definierten API-Informationen und externen Dokumentationsdetails
     */
    /* @Bean
    public OpenAPI diplomarbeitOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Diplomarbeit API")
                        .description("Dokumentation der Diplomarbeit REST API")
                        .version("v1.2")); // TODO: Version überprüfen build gradle

    } */

    @Bean
    public OperationCustomizer operationCustomizer() {
        // add error type to each operation
        return (operation, handlerMethod) -> {
            operation.getResponses().addApiResponse("401", new ApiResponse().description("Login required"));
            operation.getResponses().addApiResponse("403", new ApiResponse().description("Not allowed for your role or X-CSRF-TOKEN not set"));
            operation.getResponses().addApiResponse("400", new ApiResponse().description("Parameter/RequestBody validation error"));
            operation.getResponses().addApiResponse("404", new ApiResponse().description("Not found"));
            operation.getResponses().addApiResponse("500", new ApiResponse().description("Any other error"));
            operation.getResponses().addApiResponse("503", new ApiResponse().description("No connection with AD LDAP possible, likely not connected to the VPN."));
            return operation;
        };
    }
}
