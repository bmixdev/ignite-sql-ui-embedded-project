package com.example.igniteapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Ignite SQL UI API",
                version = "1.1.0",
                description = "REST API для выполнения SQL в Apache Ignite (thin JDBC) и получения диагностической информации.",
                contact = @Contact(name = "Project maintainer"),
                license = @License(name = "Apache-2.0")
        )
)
public class OpenApiConfig {
}
