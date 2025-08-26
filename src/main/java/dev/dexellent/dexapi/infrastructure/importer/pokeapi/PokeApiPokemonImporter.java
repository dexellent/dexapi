package dev.dexellent.dexapi.infrastructure.importer.pokeapi;

import dev.dexellent.dexapi.domain.model.*;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.repository.PokemonRepository;
import dev.dexellent.dexapi.infrastructure.importer.DataImporter;
import dev.dexellent.dexapi.infrastructure.importer.ImportResult;
import dev.dexellent.dexapi.infrastructure.importer.config.ImportConfig;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiName;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiPokemonResponse;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiSpeciesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PokeApiPokemonImporter implements DataImporter<Pokemon> {

    private final PokeApiClient pokeApiClient;
    private final PokemonRepository pokemonRepository;
    private final ImportConfig importConfig;

    @Override
    public String getSourceName() {
        return "PokeAPI v2 - Pokemon";
    }

    @Override
    public List<Pokemon> importData(int limit, int offset) {
        List<Pokemon> importedPokemon = new ArrayList<>();

        for (int i = offset + 1; i <= offset + limit; i++) {
            try {
                Pokemon imported = importSinglePokemon(i);
                if (imported != null) {
                    importedPokemon.add(imported);
                }

                Thread.sleep(150); // Rate limiting

            } catch (Exception e) {
                log.error("Failed to import Pokemon #{}: {}", i, e.getMessage());
            }
        }

        return importedPokemon;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pokemon importSinglePokemon(int pokemonId) {
        try {
            log.debug("Importing Pokemon #{}", pokemonId);

            if (pokemonRepository.findByNationalDexNumber(pokemonId).isPresent()) {
                log.debug("Pokemon #{} already exists, skipping", pokemonId);
                return null;
            }

            PokeApiPokemonResponse pokemonData = pokeApiClient.getPokemon(pokemonId);
            if (pokemonData == null) {
                log.warn("No data received for Pokemon #{}", pokemonId);
                return null;
            }

            PokeApiSpeciesResponse speciesData = pokeApiClient.getSpecies(pokemonId);

            Pokemon pokemon = mapToPokemon(pokemonData, speciesData);
            Pokemon savedPokemon = pokemonRepository.save(pokemon);

            // Add translations AFTER saving the Pokemon entity
            addTranslations(savedPokemon, pokemonData, speciesData);
            pokemonRepository.save(savedPokemon);

            log.info("Successfully imported Pokemon #{}: {}", pokemonId, pokemon.getIdentifier());
            return savedPokemon;

        } catch (Exception e) {
            log.error("Failed to import Pokemon #{}: {}", pokemonId, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isHealthy() {
        return importConfig.getPokeApi().isEnabled() && pokeApiClient.isHealthy();
    }

    @Override
    public ImportResult validateData(List<Pokemon> data) {
        List<String> errors = new ArrayList<>();
        int validCount = 0;

        for (Pokemon pokemon : data) {
            if (pokemon.getNationalDexNumber() == null || pokemon.getNationalDexNumber() <= 0) {
                errors.add("Invalid national dex number for: " + pokemon.getIdentifier());
                continue;
            }

            if (pokemon.getIdentifier() == null || pokemon.getIdentifier().trim().isEmpty()) {
                errors.add("Missing identifier for Pokemon #" + pokemon.getNationalDexNumber());
                continue;
            }

            if (pokemon.getHp() == null || pokemon.getAttack() == null ||
                    pokemon.getDefense() == null || pokemon.getSpecialAttack() == null ||
                    pokemon.getSpecialDefense() == null || pokemon.getSpeed() == null) {
                errors.add("Missing stats for: " + pokemon.getIdentifier());
                continue;
            }

            validCount++;
        }

        return ImportResult.builder()
                .success(errors.isEmpty())
                .totalRecords(data.size())
                .successfulImports(validCount)
                .failedImports(data.size() - validCount)
                .errors(errors)
                .source(getSourceName())
                .build();
    }

    private Pokemon mapToPokemon(PokeApiPokemonResponse pokemonData, PokeApiSpeciesResponse speciesData) {
        Pokemon pokemon = Pokemon.builder()
                .nationalDexNumber(pokemonData.getId().intValue())
                .identifier(pokemonData.getName())
                .baseExperience(pokemonData.getBaseExperience())
                .height(pokemonData.getHeight() != null ?
                        new BigDecimal(pokemonData.getHeight()).divide(new BigDecimal(10)) : null)
                .weight(pokemonData.getWeight() != null ?
                        new BigDecimal(pokemonData.getWeight()).divide(new BigDecimal(10)) : null)
                .build();

        // Map stats
        if (pokemonData.getStats() != null) {
            for (PokeApiPokemonResponse.PokemonStat stat : pokemonData.getStats()) {
                String statName = stat.getStat().getName();
                Integer baseStat = stat.getBaseStat();

                switch (statName) {
                    case "hp" -> pokemon.setHp(baseStat);
                    case "attack" -> pokemon.setAttack(baseStat);
                    case "defense" -> pokemon.setDefense(baseStat);
                    case "special-attack" -> pokemon.setSpecialAttack(baseStat);
                    case "special-defense" -> pokemon.setSpecialDefense(baseStat);
                    case "speed" -> pokemon.setSpeed(baseStat);
                }
            }
        }

        // Map species data
        if (speciesData != null) {
            pokemon.setCaptureRate(speciesData.getCaptureRate());
            pokemon.setGrowthRate(speciesData.getGrowthRate() != null ?
                    speciesData.getGrowthRate().getName() : null);
            pokemon.setEggCycles(speciesData.getHatchCounter());
            pokemon.setColor(speciesData.getColor() != null ?
                    speciesData.getColor().getName() : null);
            pokemon.setShape(speciesData.getShape() != null ?
                    speciesData.getShape().getName() : null);

            // Calculate gender ratio
            if (speciesData.getGenderRate() != null) {
                int genderRate = speciesData.getGenderRate();
                if (genderRate == -1) {
                    pokemon.setGenderRatio("genderless");
                } else {
                    double femaleRatio = (genderRate / 8.0) * 100;
                    double maleRatio = 100 - femaleRatio;
                    pokemon.setGenderRatio(String.format("%.1f%% male, %.1f%% female",
                            maleRatio, femaleRatio));
                }
            }
        }

        // Set default values for required stats if missing
        if (pokemon.getHp() == null) pokemon.setHp(1);
        if (pokemon.getAttack() == null) pokemon.setAttack(1);
        if (pokemon.getDefense() == null) pokemon.setDefense(1);
        if (pokemon.getSpecialAttack() == null) pokemon.setSpecialAttack(1);
        if (pokemon.getSpecialDefense() == null) pokemon.setSpecialDefense(1);
        if (pokemon.getSpeed() == null) pokemon.setSpeed(1);

        return pokemon;
    }

    // Enhanced translation method
    private void addTranslations(Pokemon savedPokemon, PokeApiPokemonResponse pokemonData,
                                 PokeApiSpeciesResponse speciesData) {
        if (speciesData == null || speciesData.getNames() == null) {
            // Add at least English translation
            PokemonTranslation englishTranslation = PokemonTranslation.builder()
                    .pokemon(savedPokemon)
                    .language(Language.EN)
                    .name(capitalizeFirst(pokemonData.getName()))
                    .build();

            savedPokemon.setTranslations(List.of(englishTranslation));
            return;
        }

        List<PokemonTranslation> translations = new ArrayList<>();

        // English translation (default)
        PokemonTranslation englishTranslation = PokemonTranslation.builder()
                .pokemon(savedPokemon)
                .language(Language.EN)
                .name(capitalizeFirst(pokemonData.getName()))
                .build();

        // Add genus and description for English
        if (speciesData.getGenera() != null) {
            speciesData.getGenera().stream()
                    .filter(genus -> "en".equals(genus.getLanguage().getName()))
                    .findFirst()
                    .ifPresent(genus -> englishTranslation.setSpecies(genus.getGenus()));
        }

        if (speciesData.getFlavorTextEntries() != null) {
            speciesData.getFlavorTextEntries().stream()
                    .filter(entry -> "en".equals(entry.getLanguage().getName()))
                    .findFirst()
                    .ifPresent(entry -> englishTranslation.setDescription(
                            cleanFlavorText(entry.getFlavorText())
                    ));
        }

        translations.add(englishTranslation);

        // Add other language translations
        for (PokeApiName name : speciesData.getNames()) {
            Language language = mapLanguage(name.getLanguage().getName());
            if (language != null && language != Language.EN) {
                PokemonTranslation translation = PokemonTranslation.builder()
                        .pokemon(savedPokemon)
                        .language(language)
                        .name(name.getName())
                        .build();

                // Add genus for this language if available
                if (speciesData.getGenera() != null) {
                    speciesData.getGenera().stream()
                            .filter(genus -> name.getLanguage().getName().equals(genus.getLanguage().getName()))
                            .findFirst()
                            .ifPresent(genus -> translation.setSpecies(genus.getGenus()));
                }

                // Add flavor text for this language if available
                if (speciesData.getFlavorTextEntries() != null) {
                    speciesData.getFlavorTextEntries().stream()
                            .filter(entry -> name.getLanguage().getName().equals(entry.getLanguage().getName()))
                            .findFirst()
                            .ifPresent(entry -> translation.setDescription(
                                    cleanFlavorText(entry.getFlavorText())
                            ));
                }

                translations.add(translation);
            }
        }

        savedPokemon.setTranslations(translations);
    }

    private Language mapLanguage(String pokeApiLanguage) {
        return switch (pokeApiLanguage) {
            case "en" -> Language.EN;
            case "fr" -> Language.FR;
            case "ja", "ja-Hrkt" -> Language.JA;
            case "es" -> Language.ES;
            case "de" -> Language.DE;
            case "it" -> Language.IT;
            case "ko" -> Language.KO;
            case "zh", "zh-Hant", "zh-Hans" -> Language.ZH;
            default -> null;
        };
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String cleanFlavorText(String flavorText) {
        if (flavorText == null) return null;
        return flavorText.replace("\f", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}