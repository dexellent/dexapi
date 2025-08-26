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
    private NamedResource growthRate;

    private NamedResource color;
    private NamedResource shape;
    private NamedResource habitat;
    private NamedResource generation;

    @JsonProperty("evolves_from_species")
    private NamedResource evolvesFromSpecies;

    @JsonProperty("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    private List<Name> names;
    private List<Genus> genera;

    @Data
    public static class FlavorTextEntry {
        @JsonProperty("flavor_text")
        private String flavorText;

        private NamedResource language;
        private NamedResource version;
    }

    @Data
    public static class Name {
        private String name;
        private NamedResource language;
    }

    @Data
    public static class Genus {
        private String genus;
        private NamedResource language;
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