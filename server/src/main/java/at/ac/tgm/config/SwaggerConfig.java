package at.ac.tgm.config;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String INFO_ANY_ENDPOINT = " (Can happen on any endpoint)";
    
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
