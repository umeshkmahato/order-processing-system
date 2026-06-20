package com.orderprocessing.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Order Processing API",
                version = "v1",
                description = "REST APIs for e-commerce order processing",
                contact = @Contact(name = "Order Processing Team", email = "support@orderprocessing.com"),
                license = @License(name = "Apache 2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
                @Server(url = "https://dev.orderprocessing.com", description = "Dev"),
                @Server(url = "https://api.orderprocessing.com", description = "Prod")
        }
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi customerApi() {
        return GroupedOpenApi.builder()
                .group("customer")
                .pathsToMatch("/api/orders/**")
                .pathsToExclude("/api/orders/*/status")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/orders/*/status", "/api/admin/**")
                .build();
    }
}

