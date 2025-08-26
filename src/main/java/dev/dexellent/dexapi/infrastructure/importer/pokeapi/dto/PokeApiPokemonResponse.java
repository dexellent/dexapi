package dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PokeApiPokemonResponse {
    private Long id;
    private String name;
    private Integer height;
    private Integer weight;

    @JsonProperty("base_experience")
    private Integer baseExperience;

    private Integer order;

    @JsonProperty("is_default")
    private Boolean isDefault;

    private List<PokemonStat> stats;
    private List<PokemonType> types;
    private List<PokemonAbility> abilities;
    private PokemonSpecies species;
    private PokemonSprites sprites;

    @Data
    public static class PokemonStat {
        @JsonProperty("base_stat")
        private Integer baseStat;

        private Integer effort;
        private NamedResource stat;
    }

    @Data
    public static class PokemonType {
        private Integer slot;
        private NamedResource type;
    }

    @Data
    public static class PokemonAbility {
        @JsonProperty("is_hidden")
        private Boolean isHidden;

        private Integer slot;
        private NamedResource ability;
    }

    @Data
    public static class PokemonSpecies {
        private String name;
        private String url;
    }

    @Data
    public static class PokemonSprites {
        @JsonProperty("front_default")
        private String frontDefault;

        @JsonProperty("back_default")
        private String backDefault;

        @JsonProperty("front_shiny")
        private String frontShiny;

        @JsonProperty("back_shiny")
        private String backShiny;
    }

    @Data
    public static class NamedResource {
        private String name;
        private String url;

        public Long extractId() {
            if (url == null) return null;
            String[] parts = url.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (!parts[i].isEmpty()) {
                    try {
                        return Long.parseLong(parts[i]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
            return null;
        }
    }
}