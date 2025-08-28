package dev.dexellent.dexapi.infrastructure.importer.pokeapi;

import dev.dexellent.dexapi.domain.model.*;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.repository.GenerationRepository;
import dev.dexellent.dexapi.domain.repository.PokemonRepository;
import dev.dexellent.dexapi.domain.repository.TypeRepository;
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
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PokeApiPokemonImporter implements DataImporter<Pokemon> {

    private final PokeApiClient pokeApiClient;
    private final PokemonRepository pokemonRepository;
    private final TypeRepository typeRepository;
    private final GenerationRepository generationRepository;
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

            // Check if Pokemon already exists
            Optional<Pokemon> existingPokemon = pokemonRepository.findByNationalDexNumber(pokemonId);
            if (existingPokemon.isPresent()) {
                Pokemon pokemon = existingPokemon.get();

                // Check if it already has translations
                if (pokemon.getTranslations() != null && !pokemon.getTranslations().isEmpty()) {
                    log.debug("Pokemon #{} already exists with translations, skipping", pokemonId);
                    return null;
                } else {
                    log.debug("Pokemon #{} exists but has no translations, will add them", pokemonId);
                    // Continue with the import to add translations
                }
            }

            PokeApiPokemonResponse pokemonData = pokeApiClient.getPokemon(pokemonId);
            if (pokemonData == null) {
                log.warn("No data received for Pokemon #{}", pokemonId);
                return null;
            }

            PokeApiSpeciesResponse speciesData = pokeApiClient.getSpecies(pokemonId);

            Pokemon pokemon;
            if (existingPokemon.isPresent()) {
                // Use existing Pokemon, just add translations and types
                pokemon = existingPokemon.get();

                // Add types if they don't exist
                if (pokemon.getTypes() == null || pokemon.getTypes().isEmpty()) {
                    addTypes(pokemon, pokemonData);
                }

                // Add translations
                addTranslations(pokemon, pokemonData, speciesData);
                pokemon = pokemonRepository.save(pokemon);
            } else {
                // Create new Pokemon
                pokemon = mapToPokemon(pokemonData, speciesData);
                linkGeneration(pokemon, speciesData);
                Pokemon savedPokemon = pokemonRepository.save(pokemon);

                // Add types AFTER saving the Pokemon entity
                addTypes(savedPokemon, pokemonData);

                // Add translations AFTER saving the Pokemon entity
                addTranslations(savedPokemon, pokemonData, speciesData);

                pokemon = pokemonRepository.save(savedPokemon);
            }

            log.info("Successfully imported Pokemon #{}: {}", pokemonId, pokemon.getIdentifier());
            return pokemon;

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

    private void linkGeneration(Pokemon pokemon, PokeApiSpeciesResponse speciesData) {
        if (speciesData == null || speciesData.getGeneration() == null) {
            // Fallback to generation based on national dex number
            int genNumber = determineGenerationByDexNumber(pokemon.getNationalDexNumber());
            generationRepository.findByNumber(genNumber)
                    .ifPresent(pokemon::setGeneration);
            return;
        }

        // Extract generation number from URL
        Long generationId = extractIdFromUrl(speciesData.getGeneration().getUrl());
        if (generationId != null) {
            generationRepository.findByNumber(generationId.intValue())
                    .ifPresent(pokemon::setGeneration);
        }
    }

    private void addTypes(Pokemon savedPokemon, PokeApiPokemonResponse pokemonData) {
        if (pokemonData.getTypes() == null || pokemonData.getTypes().isEmpty()) {
            return;
        }

        List<PokemonType> pokemonTypes = new ArrayList<>();

        for (PokeApiPokemonResponse.PokemonType pokeApiType : pokemonData.getTypes()) {
            String typeName = pokeApiType.getType().getName();

            typeRepository.findByIdentifier(typeName).ifPresent(type -> {
                PokemonType pokemonType = PokemonType.builder()
                        .pokemon(savedPokemon)
                        .type(type)
                        .slot(pokeApiType.getSlot())
                        .build();

                pokemonTypes.add(pokemonType);
            });
        }

        savedPokemon.setTypes(pokemonTypes);
    }

    private void addTranslations(Pokemon savedPokemon, PokeApiPokemonResponse pokemonData,
                                 PokeApiSpeciesResponse speciesData) {
        if (speciesData == null || speciesData.getNames() == null) {
            // Add at least English translation if it doesn't exist
            addTranslationIfNotExists(savedPokemon, Language.EN, capitalizeFirst(pokemonData.getName()),
                    null, null, null);
            return;
        }

        // Use a Map to avoid duplicate languages and handle existing translations
        Map<Language, PokemonTranslation> existingTranslations = new HashMap<>();

        // Load existing translations into map
        if (savedPokemon.getTranslations() != null) {
            for (PokemonTranslation existing : savedPokemon.getTranslations()) {
                existingTranslations.put(existing.getLanguage(), existing);
            }
        }

        // English translation (default)
        if (!existingTranslations.containsKey(Language.EN)) {
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

            existingTranslations.put(Language.EN, englishTranslation);
        }

        // Add other language translations
        for (PokeApiName name : speciesData.getNames()) {
            Language language = Util.mapLanguage(name.getLanguage().getName());
            if (language != null && !existingTranslations.containsKey(language)) {
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

                existingTranslations.put(language, translation);
            } else if (language != null) {
                log.debug("Translation for language {} already exists for Pokemon #{}, skipping",
                        language.getCode(), savedPokemon.getNationalDexNumber());
            }
        }

        // Convert map values to list and set on Pokemon
        List<PokemonTranslation> allTranslations = new ArrayList<>(existingTranslations.values());
        savedPokemon.setTranslations(allTranslations);

        log.debug("Pokemon #{} now has {} translations", savedPokemon.getNationalDexNumber(), allTranslations.size());
    }

    private void addTranslationIfNotExists(Pokemon pokemon, Language language, String name,
                                           String species, String description, String habitat) {
        // Check if translation already exists
        if (pokemon.getTranslations() != null) {
            boolean exists = pokemon.getTranslations().stream()
                    .anyMatch(t -> t.getLanguage() == language);
            if (exists) {
                log.debug("Translation for language {} already exists for Pokemon #{}, skipping",
                        language.getCode(), pokemon.getNationalDexNumber());
                return;
            }
        }

        PokemonTranslation translation = PokemonTranslation.builder()
                .pokemon(pokemon)
                .language(language)
                .name(name)
                .species(species)
                .description(description)
                .habitat(habitat)
                .build();

        if (pokemon.getTranslations() == null) {
            pokemon.setTranslations(new ArrayList<>());
        }
        pokemon.getTranslations().add(translation);
    }

    private int determineGenerationByDexNumber(int nationalDexNumber) {
        if (nationalDexNumber <= 151) return 1;
        if (nationalDexNumber <= 251) return 2;
        if (nationalDexNumber <= 386) return 3;
        if (nationalDexNumber <= 493) return 4;
        if (nationalDexNumber <= 649) return 5;
        if (nationalDexNumber <= 721) return 6;
        if (nationalDexNumber <= 809) return 7;
        if (nationalDexNumber <= 905) return 8;
        return 9; // Gen 9 and beyond
    }

    private Long extractIdFromUrl(String url) {
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
