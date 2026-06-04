package com.unsocial.unsocial.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, jwtSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("UnSocial API")
                .description("""
                        **UnSocial — Personal Safety and Social Escape Platform**
                        
                        ## How to authenticate
                        1. Use `POST /api/auth/register` to create an account
                        2. Use `POST /api/auth/login` to get your JWT token
                        3. Click **Authorize** (top right), paste the token and click **Authorize**
                        4. All protected endpoints will now include your token automatically
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("UnSocial Dev Team")
                        .email("dev@unsocial.com"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste your JWT token here (without the 'Bearer ' prefix)");
    }
}