package dev.dexellent.dexapi.infrastructure.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DexAPI - Multilingual Pokemon REST API")
                        .description("A comprehensive RESTful Pokemon API with multilingual support. " +
                                "Provides detailed Pokemon data including stats, moves, abilities, and types " +
                                "across multiple languages (English, French, Japanese, Spanish, German).")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("DexAPI Team")
                                .url("https://github.com/dexellent/dexapi")
                                .email("support@dexellent.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development server"),
                        new Server()
                                .url("https://api.dexellent.dev")
                                .description("Production server")
                ));
    }
}
