package dev.dexellent.dexapi.infrastructure.importer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dexapi.import")
public class ImportConfig {
    private int defaultBatchSize = 20;
    private int maxRetries = 3;
    private long retryDelayMs = 1000;
    private boolean enableWebInterface = true;

    private PokeApi pokeApi = new PokeApi();

    @Data
    public static class PokeApi {
        private String baseUrl = "https://pokeapi.co/api/v2";
        private int timeoutMs = 30000;
        private boolean enabled = true;
    }
}