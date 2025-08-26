package dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PokeApiGenerationResponse {
    private Long id;
    private String name;
    private List<PokeApiNamedResource> abilities;

    @JsonProperty("main_region")
    private PokeApiNamedResource mainRegion;

    private List<PokeApiNamedResource> moves;
    private List<PokeApiName> names;

    @JsonProperty("pokemon_species")
    private List<PokeApiNamedResource> pokemonSpecies;

    private List<PokeApiNamedResource> types;

    @JsonProperty("version_groups")
    private List<PokeApiNamedResource> versionGroups;
}