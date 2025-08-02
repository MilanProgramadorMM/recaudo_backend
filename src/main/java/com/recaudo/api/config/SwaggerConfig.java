package com.recaudo.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(createSecurityRequirement())
                .components(
                        new Components()
                                .addSecuritySchemes("Bearer Authentication", createJWTTokenScheme())
                                .addSecuritySchemes("Api Key", createAPIKeyScheme())
                                .addSecuritySchemes("Application ID", createAppIDScheme())
                )
                .info(new Info().title("KATA Authentication API")
                        .description("Management of users, auth and register cases.")
                        .version("1.0").contact(new Contact().name("Carlos Marquez")
                                .email("example@test.com").url("https://www.example.com"))
                        .license(new License().name("License of API")
                                .url("API license URL")));
    }

    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement()
                .addList("Bearer Authentication")
                .addList("Api Key")
                .addList("Application ID");
    }

    private SecurityScheme createJWTTokenScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("api-key");
    }

    private SecurityScheme createAppIDScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("application-id");
    }
}
