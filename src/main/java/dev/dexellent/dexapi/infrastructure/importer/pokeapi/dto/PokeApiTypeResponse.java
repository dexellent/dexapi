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

    @JsonProperty("past_damage_relations")
    private List<TypeRelationsPast> pastDamageRelations;

    @JsonProperty("game_indices")
    private List<GameIndex> gameIndices;

    private PokeApiNamedResource generation;

    @JsonProperty("move_damage_class")
    private PokeApiNamedResource moveDamageClass;

    private List<TypePokemon> pokemon;
    private List<PokeApiNamedResource> moves;

    @Data
    public static class TypeRelations {
        @JsonProperty("no_damage_to")
        private List<PokeApiNamedResource> noDamageTo;

        @JsonProperty("half_damage_to")
        private List<PokeApiNamedResource> halfDamageTo;

        @JsonProperty("double_damage_to")
        private List<PokeApiNamedResource> doubleDamageTo;

        @JsonProperty("no_damage_from")
        private List<PokeApiNamedResource> noDamageFrom;

        @JsonProperty("half_damage_from")
        private List<PokeApiNamedResource> halfDamageFrom;

        @JsonProperty("double_damage_from")
        private List<PokeApiNamedResource> doubleDamageFrom;
    }

    @Data
    public static class TypeRelationsPast {
        private PokeApiNamedResource generation;

        @JsonProperty("damage_relations")
        private TypeRelations damageRelations;
    }

    @Data
    public static class TypePokemon {
        private Integer slot;
        private PokeApiNamedResource pokemon;
    }

    @Data
    public static class GameIndex {
        @JsonProperty("game_index")
        private Integer gameIndex;

        private PokeApiNamedResource generation;
    }
}
