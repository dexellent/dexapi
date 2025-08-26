package dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto;

import lombok.Data;

@Data
public class PokeApiName {
    private String name;
    private PokeApiNamedResource language;
}
