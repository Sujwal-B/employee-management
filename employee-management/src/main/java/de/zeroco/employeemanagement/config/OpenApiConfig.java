package de.zeroco.employeemanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
            .info(new Info().title("Employee Management API")
                .version("v1.0")
                .description("API for managing employees, departments, and projects. " +
                             "Provides functionalities for CRUD operations on these entities, " +
                             "along with authentication and authorization using JWT."))
            .addSecuritySchemes(securitySchemeName,
                new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token obtained from the /auth/login endpoint. Enter in the format: Bearer <token>")
            )
            .addSecurityRequirement(new SecurityRequirement().addList(securitySchemeName));
    }
}
