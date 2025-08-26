package dev.dexellent.dexapi.infrastructure.importer.pokeapi;

import dev.dexellent.dexapi.infrastructure.importer.config.ImportConfig;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiPokemonResponse;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiSpeciesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
@Slf4j
public class PokeApiClient {

    private final ImportConfig importConfig;
    private final RestTemplate restTemplate;

    public PokeApiClient(ImportConfig importConfig) {
        this.importConfig = importConfig;
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(importConfig.getPokeApi().getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(importConfig.getPokeApi().getTimeoutMs()))
                .build();
    }

    public PokeApiPokemonResponse getPokemon(int id) {
        String url = importConfig.getPokeApi().getBaseUrl() + "/pokemon/" + id;
        try {
            return restTemplate.getForObject(url, PokeApiPokemonResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch Pokemon with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public PokeApiSpeciesResponse getSpecies(int id) {
        String url = importConfig.getPokeApi().getBaseUrl() + "/pokemon-species/" + id;
        try {
            return restTemplate.getForObject(url, PokeApiSpeciesResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch Species with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public boolean isHealthy() {
        try {
            String url = importConfig.getPokeApi().getBaseUrl() + "/pokemon/1";
            restTemplate.headForHeaders(url);
            return true;
        } catch (Exception e) {
            log.warn("PokeAPI health check failed: {}", e.getMessage());
            return false;
        }
    }
}
