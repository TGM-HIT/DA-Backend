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
    private static final String INFO_ANY_ENDPOINT = " (Can happen on any endpoint)";
    
    /**
     * Erstellt und konfiguriert die OpenAPI-Dokumentation.
     *
     * @return das OpenAPI-Objekt mit den definierten API-Informationen und externen Dokumentationsdetails
     */
    @Bean
    public OpenAPI diplomarbeitOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("API Dokumentation")
                        .description("Dokumentation der REST API")
                        .version("v1.0")); // TODO: Version überprüfen build gradle

    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        // add error type to each operation
        return (operation, handlerMethod) -> {
            operation.getResponses().addApiResponse("401", new ApiResponse().description("Login required" + INFO_ANY_ENDPOINT));
            operation.getResponses().addApiResponse("403", new ApiResponse().description("Not allowed for your role or X-CSRF-TOKEN not set" + INFO_ANY_ENDPOINT));
            operation.getResponses().addApiResponse("400", new ApiResponse().description("Parameter/RequestBody validation error" + INFO_ANY_ENDPOINT));
            operation.getResponses().addApiResponse("404", new ApiResponse().description("Not found" + INFO_ANY_ENDPOINT));
            operation.getResponses().addApiResponse("500", new ApiResponse().description("Any other error" + INFO_ANY_ENDPOINT));
            operation.getResponses().addApiResponse("503", new ApiResponse().description("No connection with AD LDAP possible, likely not connected to the VPN." + INFO_ANY_ENDPOINT));
            return operation;
        };
    }
}
