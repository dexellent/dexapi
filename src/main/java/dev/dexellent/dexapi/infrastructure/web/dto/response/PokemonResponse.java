package dev.dexellent.dexapi.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PokemonResponse {

    private Long id;

    @JsonProperty("national_dex_number")
    private Integer nationalDexNumber;

    private String identifier;

    // Localized fields
    private String name;
    private String species;
    private String description;
    private String habitat;

    // Stats
    private StatsResponse stats;

    // Physical characteristics
    private BigDecimal height;
    private BigDecimal weight;

    @JsonProperty("capture_rate")
    private Integer captureRate;

    @JsonProperty("base_experience")
    private Integer baseExperience;

    @JsonProperty("growth_rate")
    private String growthRate;

    @JsonProperty("gender_ratio")
    private String genderRatio;

    @JsonProperty("egg_cycles")
    private Integer eggCycles;

    private String color;
    private String shape;

    // Related data
    private List<TypeResponse> types;
    private List<AbilityResponse> abilities;
    private GenerationResponse generation;

    // Localization metadata
    private String language;

    @JsonProperty("available_languages")
    private List<String> availableLanguages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StatsResponse {
        private Integer hp;
        private Integer attack;
        private Integer defense;

        @JsonProperty("special_attack")
        private Integer specialAttack;

        @JsonProperty("special_defense")
        private Integer specialDefense;

        private Integer speed;

        @JsonProperty("total")
        public Integer getTotal() {
            if (hp == null || attack == null || defense == null ||
                    specialAttack == null || specialDefense == null || speed == null) {
                return null;
            }
            return hp + attack + defense + specialAttack + specialDefense + speed;
        }
    }
}