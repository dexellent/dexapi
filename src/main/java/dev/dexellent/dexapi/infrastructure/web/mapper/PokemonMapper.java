package dev.dexellent.dexapi.infrastructure.web.mapper;

import dev.dexellent.dexapi.domain.model.*;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.infrastructure.web.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class PokemonMapper {

    public PokemonResponse toResponse(Pokemon pokemon, Language language) {
        if (pokemon == null) return null;

        PokemonTranslation translation = pokemon.getTranslationOrDefault(language);

        return PokemonResponse.builder()
                .id(pokemon.getId())
                .nationalDexNumber(pokemon.getNationalDexNumber())
                .identifier(pokemon.getIdentifier())
                .name(translation != null ? translation.getName() : pokemon.getIdentifier())
                .species(translation != null ? translation.getSpecies() : null)
                .description(translation != null ? translation.getDescription() : null)
                .habitat(translation != null ? translation.getHabitat() : pokemon.getShape())
                .stats(PokemonResponse.StatsResponse.builder()
                        .hp(pokemon.getHp())
                        .attack(pokemon.getAttack())
                        .defense(pokemon.getDefense())
                        .specialAttack(pokemon.getSpecialAttack())
                        .specialDefense(pokemon.getSpecialDefense())
                        .speed(pokemon.getSpeed())
                        .build())
                .height(pokemon.getHeight())
                .weight(pokemon.getWeight())
                .captureRate(pokemon.getCaptureRate())
                .baseExperience(pokemon.getBaseExperience())
                .growthRate(pokemon.getGrowthRate())
                .genderRatio(pokemon.getGenderRatio())
                .eggCycles(pokemon.getEggCycles())
                .color(pokemon.getColor())
                .shape(pokemon.getShape())
                .types(mapTypes(pokemon.getTypes(), language))
                .abilities(mapAbilities(pokemon.getAbilities(), language))
                .generation(mapGeneration(pokemon.getGeneration()))
                .language(language.getCode())
                .build();
    }

    private List<TypeResponse> mapTypes(List<PokemonType> pokemonTypes, Language language) {
        if (pokemonTypes == null) return null;

        return pokemonTypes.stream()
                .sorted(Comparator.comparingInt(PokemonType::getSlot))
                .map(pt -> {
                    Type type = pt.getType();
                    TypeTranslation typeTranslation = type.getTranslationOrDefault(language);

                    return TypeResponse.builder()
                            .id(type.getId())
                            .identifier(type.getIdentifier())
                            .name(typeTranslation != null ? typeTranslation.getName() : type.getIdentifier())
                            .color(type.getColor())
                            .slot(pt.getSlot())
                            .build();
                })
                .toList();
    }

    private List<AbilityResponse> mapAbilities(List<PokemonAbility> pokemonAbilities, Language language) {
        if (pokemonAbilities == null) return null;

        return pokemonAbilities.stream()
                .sorted(Comparator.comparingInt(pa -> pa.getSlot() != null ? pa.getSlot() : 0))
                .map(pa -> {
                    Ability ability = pa.getAbility();
                    AbilityTranslation abilityTranslation = ability.getTranslationOrDefault(language);

                    return AbilityResponse.builder()
                            .id(ability.getId())
                            .identifier(ability.getIdentifier())
                            .name(abilityTranslation != null ? abilityTranslation.getName() : ability.getIdentifier())
                            .description(abilityTranslation != null ? abilityTranslation.getDescription() : null)
                            .effect(abilityTranslation != null ? abilityTranslation.getEffect() : null)
                            .shortEffect(abilityTranslation != null ? abilityTranslation.getShortEffect() : null)
                            .isHidden(pa.getIsHidden())
                            .slot(pa.getSlot())
                            .build();
                })
                .toList();
    }

    private GenerationResponse mapGeneration(Generation generation) {
        if (generation == null) return null;

        return GenerationResponse.builder()
                .id(generation.getId())
                .number(generation.getNumber())
                .name(generation.getName())
                .region(generation.getRegion())
                .releaseYear(generation.getReleaseYear())
                .games(generation.getGames())
                .build();
    }
}