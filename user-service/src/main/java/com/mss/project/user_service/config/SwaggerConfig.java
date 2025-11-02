package com.mss.project.user_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "User Services API for 4Bus Microservices", version = "1.0", description = "API for User Services in 4Bus Microservices application", contact = @Contact(name = "4Bus Team", email = "4bus@gmail.com")), servers = {
                @Server(url = "https://user.4bus.io.vn", description = "Production Server"),
                @Server(url = "http://localhost:8080", description = "Local Development Server")
})
@SecurityScheme(name = "Bearer Authentication", description = "JWT Token Authentication", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class SwaggerConfig {
}
