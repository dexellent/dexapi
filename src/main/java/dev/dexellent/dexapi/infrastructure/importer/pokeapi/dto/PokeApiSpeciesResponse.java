package dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PokeApiSpeciesResponse {
    private Long id;
    private String name;
    private Integer order;

    @JsonProperty("gender_rate")
    private Integer genderRate;

    @JsonProperty("capture_rate")
    private Integer captureRate;

    @JsonProperty("base_happiness")
    private Integer baseHappiness;

    @JsonProperty("is_baby")
    private Boolean isBaby;

    @JsonProperty("is_legendary")
    private Boolean isLegendary;

    @JsonProperty("is_mythical")
    private Boolean isMythical;

    @JsonProperty("hatch_counter")
    private Integer hatchCounter;

    @JsonProperty("has_gender_differences")
    private Boolean hasGenderDifferences;

    @JsonProperty("forms_switchable")
    private Boolean formsSwitchable;

    @JsonProperty("growth_rate")
    private PokeApiNamedResource growthRate;

    private PokeApiNamedResource color;
    private PokeApiNamedResource shape;
    private PokeApiNamedResource habitat;
    private PokeApiNamedResource generation;

    @JsonProperty("evolves_from_species")
    private PokeApiNamedResource evolvesFromSpecies;

    @JsonProperty("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    private List<PokeApiName> names;
    private List<Genus> genera;

    @Data
    public static class FlavorTextEntry {
        @JsonProperty("flavor_text")
        private String flavorText;

        private PokeApiNamedResource language;
        private PokeApiNamedResource version;
    }

    @Data
    public static class Genus {
        private String genus;
        private PokeApiNamedResource language;
    }
}