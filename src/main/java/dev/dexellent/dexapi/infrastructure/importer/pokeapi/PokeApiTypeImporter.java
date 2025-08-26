package dev.dexellent.dexapi.infrastructure.importer.pokeapi;

import dev.dexellent.dexapi.domain.model.Type;
import dev.dexellent.dexapi.domain.model.TypeTranslation;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.repository.GenerationRepository;
import dev.dexellent.dexapi.domain.repository.TypeRepository;
import dev.dexellent.dexapi.infrastructure.importer.DataImporter;
import dev.dexellent.dexapi.infrastructure.importer.ImportResult;
import dev.dexellent.dexapi.infrastructure.importer.config.ImportConfig;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiName;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiTypeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PokeApiTypeImporter implements DataImporter<Type> {

    private final PokeApiClient pokeApiClient;
    private final TypeRepository typeRepository;
    private final GenerationRepository generationRepository;
    private final ImportConfig importConfig;

    @Override
    public String getSourceName() {
        return "PokeAPI v2 - Types";
    }

    @Override
    public List<Type> importData(int limit, int offset) {
        List<Type> importedTypes = new ArrayList<>();

        // Types in PokeAPI are numbered 1-18 currently (including Shadow type)
        int maxTypes = Math.min(limit, 18);

        for (int i = offset + 1; i <= offset + maxTypes; i++) {
            if (i > 18) break; // Don't exceed known types

            try {
                Type imported = importSingleType(i);
                if (imported != null) {
                    importedTypes.add(imported);
                }

                Thread.sleep(150); // Rate limiting

            } catch (Exception e) {
                log.error("Failed to import Type #{}: {}", i, e.getMessage());
            }
        }

        return importedTypes;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Type importSingleType(int typeId) {
        try {
            log.debug("Importing Type #{}", typeId);

            // Check if type already exists
            Optional<Type> existingType = typeRepository.findById((long) typeId);
            if (existingType.isPresent()) {
                Type type = existingType.get();

                // Check if it already has translations
                if (type.getTranslations() != null && !type.getTranslations().isEmpty()) {
                    log.debug("Type #{} already exists with translations, skipping", typeId);
                    return null;
                } else {
                    log.debug("Type #{} exists but has no translations, will add them", typeId);
                    // Continue with the import to add translations
                }
            }

            PokeApiTypeResponse typeData = pokeApiClient.getType(typeId);
            if (typeData == null) {
                log.warn("No data received for Type #{}", typeId);
                return null;
            }

            Type type;
            if (existingType.isPresent()) {
                // Use existing type, just add translations
                type = existingType.get();
                addTranslations(type, typeData);
                type = typeRepository.save(type);
            } else {
                // Create new type
                type = mapToType(typeData);
                Type savedType = typeRepository.save(type);
                addTranslations(savedType, typeData);
                type = typeRepository.save(savedType);
            }

            log.info("Successfully imported Type #{}: {}", typeId, type.getIdentifier());
            return type;

        } catch (Exception e) {
            log.error("Failed to import Type #{}: {}", typeId, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isHealthy() {
        return importConfig.getPokeApi().isEnabled() && pokeApiClient.isHealthy();
    }

    @Override
    public ImportResult validateData(List<Type> data) {
        List<String> errors = new ArrayList<>();
        int validCount = 0;

        for (Type type : data) {
            if (type.getIdentifier() == null || type.getIdentifier().trim().isEmpty()) {
                errors.add("Missing identifier for Type #" + type.getId());
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

    private Type mapToType(PokeApiTypeResponse typeData) {
        Type type = Type.builder()
                .identifier(typeData.getName())
                .color(getTypeColor(typeData.getName()))
                .build();

        // Link with generation if available in the API response
        if (typeData.getGeneration() != null) {
            Long generationId = extractIdFromUrl(typeData.getGeneration().getUrl());
            if (generationId != null) {
                generationRepository.findById(generationId)
                        .ifPresentOrElse(
                                type::setGeneration,
                                () -> log.warn("Generation with ID {} not found for type {}",
                                        generationId, typeData.getName())
                        );
            }
        } else {
            // Fallback: Set generation based on type introduction
            Integer genNumber = getTypeIntroductionGeneration(typeData.getName());
            if (genNumber != null) {
                generationRepository.findByNumber(genNumber)
                        .ifPresentOrElse(
                                type::setGeneration,
                                () -> log.warn("Generation {} not found for type {}",
                                        genNumber, typeData.getName())
                        );
            }
        }

        return type;
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

    private Integer getTypeIntroductionGeneration(String typeName) {
        // Fallback mapping for when generation info isn't available from API
        return switch (typeName.toLowerCase()) {
            case "normal", "fire", "water", "electric", "grass", "ice",
                 "fighting", "poison", "ground", "flying", "psychic",
                 "bug", "rock", "ghost" -> 1; // Gen 1
            case "dragon" -> 1; // Gen 1 (Dragon was introduced in Gen 1)
            case "dark", "steel" -> 2; // Gen 2
            case "fairy" -> 6; // Gen 6
            case "shadow" -> 3; // Gen 3 (Colosseum/XD)
            default -> {
                log.debug("Unknown type for generation mapping: {}", typeName);
                yield 1; // Default to Gen 1
            }
        };
    }

    private void addTranslations(Type savedType, PokeApiTypeResponse typeData) {
        if (typeData.getNames() == null || typeData.getNames().isEmpty()) {
            // Add at least English translation
            TypeTranslation englishTranslation = TypeTranslation.builder()
                    .type(savedType)
                    .language(Language.EN)
                    .name(capitalizeFirst(typeData.getName()))
                    .build();

            savedType.setTranslations(List.of(englishTranslation));
            return;
        }

        // Use a Map to avoid duplicate languages
        Map<Language, TypeTranslation> translationsMap = new HashMap<>();

        // First, add English translation as default (in case it's not in the API response)
        TypeTranslation englishTranslation = TypeTranslation.builder()
                .type(savedType)
                .language(Language.EN)
                .name(capitalizeFirst(typeData.getName()))
                .build();
        translationsMap.put(Language.EN, englishTranslation);

        // Process API translations, potentially overriding the default English
        for (PokeApiName name : typeData.getNames()) {
            Language language = Util.mapLanguage(name.getLanguage().getName());
            if (language != null) {
                TypeTranslation translation = TypeTranslation.builder()
                        .type(savedType)
                        .language(language)
                        .name(name.getName())
                        .build();

                // This will replace any existing translation for this language
                translationsMap.put(language, translation);
            } else {
                log.debug("Skipping unsupported language: {}", name.getLanguage().getName());
            }
        }

        // Convert map values to list
        List<TypeTranslation> translations = new ArrayList<>(translationsMap.values());
        savedType.setTranslations(translations);

        log.debug("Added {} translations for type: {}", translations.size(), typeData.getName());
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String getTypeColor(String typeName) {
        // Return hex colors for Pokemon types based on common conventions
        return switch (typeName.toLowerCase()) {
            case "normal" -> "#A8A878";
            case "fire" -> "#F08030";
            case "water" -> "#6890F0";
            case "electric" -> "#F8D030";
            case "grass" -> "#78C850";
            case "ice" -> "#98D8D8";
            case "fighting" -> "#C03028";
            case "poison" -> "#A040A0";
            case "ground" -> "#E0C068";
            case "flying" -> "#A890F0";
            case "psychic" -> "#F85888";
            case "bug" -> "#A8B820";
            case "rock" -> "#B8A038";
            case "ghost" -> "#705898";
            case "dragon" -> "#7038F8";
            case "dark" -> "#705848";
            case "steel" -> "#B8B8D0";
            case "fairy" -> "#EE99AC";
            default -> "#68A090"; // Unknown type
        };
    }
}