package dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PokeApiTypeResponse {
    private Long id;
    private String name;
    private List<PokeApiName> names;

    @JsonProperty("damage_relations")
    private TypeRelations damageRelations;

    private PokeApiNamedResource generation;
    private List<PokeApiNamedResource> moves;

    @Data
    public static class TypeRelations {
        @JsonProperty("no_damage_to")
        private PokeApiNamedResource noDamageTo;

        @JsonProperty("half_damage_to")
        private PokeApiNamedResource halfTamageTo;

        @JsonProperty("double_damage_to")
        private PokeApiNamedResource doubleDamageTo;

        @JsonProperty("no_damage_from")
        private PokeApiNamedResource noDamageFrom;

        @JsonProperty("half_damage_from")
        private PokeApiNamedResource halfDamageFrom;

        @JsonProperty("double_damage_from")
        private PokeApiNamedResource doubleDamageFrom;
    }
}
