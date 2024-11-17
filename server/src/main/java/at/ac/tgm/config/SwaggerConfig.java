package at.ac.tgm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openApiSpec() {
        return new OpenAPI().components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
    
    @Bean
    public OperationCustomizer operationCustomizer() {
        // add error type to each operation
        return (operation, handlerMethod) -> {
            operation.getResponses().addApiResponse("401", new ApiResponse().description("Login required"));
            operation.getResponses().addApiResponse("403", new ApiResponse().description("Not allowed for your role"));
            operation.getResponses().addApiResponse("400", new ApiResponse().description("Parameter/RequestBody validation error"));
            operation.getResponses().addApiResponse("404", new ApiResponse().description("Not found"));
            operation.getResponses().addApiResponse("500", new ApiResponse().description("Any other error"));
            return operation;
        };
    }
}
