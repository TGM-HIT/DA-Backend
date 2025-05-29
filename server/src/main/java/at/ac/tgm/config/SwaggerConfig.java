package at.ac.tgm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${server.port}")
    private String localPort;
    
    private static final String INFO_ANY_ENDPOINT = " (Can happen on any endpoint)";
    
    /**
     * Erstellt und konfiguriert die OpenAPI-Dokumentation.
     *
     * @return das OpenAPI-Objekt mit den definierten API-Informationen und externen Dokumentationsdetails
     */
    @Bean
    public OpenAPI openAPI() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url("http://localhost:" + localPort).description("Local Development Server"));
        servers.add(new Server().url("https://da-backend.projekte.tgm.ac.at").description("Projekteserver"));
        return new OpenAPI()
                .info(new Info().title("API Dokumentation")
                        .description("Dokumentation der REST API")
                        .version("v3.1.0"))
                .servers(servers);
        
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
