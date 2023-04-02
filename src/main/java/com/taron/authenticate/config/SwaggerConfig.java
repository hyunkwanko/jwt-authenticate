package com.taron.authenticate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    public static final String BEARER_TOKEN = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(BEARER_TOKEN, securityScheme))
                .security(Collections.singletonList(new SecurityRequirement().addList(BEARER_TOKEN)))
                .info(new Info().title("Test").version("v1.0"));
    }
}
