package at.ac.tgm.config;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
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
