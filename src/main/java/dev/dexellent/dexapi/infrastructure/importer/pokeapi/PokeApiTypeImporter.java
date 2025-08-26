package dev.dexellent.dexapi.infrastructure.importer.pokeapi;

import dev.dexellent.dexapi.domain.model.Type;
import dev.dexellent.dexapi.domain.model.TypeTranslation;
import dev.dexellent.dexapi.domain.model.enums.Language;
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

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PokeApiTypeImporter implements DataImporter<Type> {

    private final PokeApiClient pokeApiClient;
    private final TypeRepository typeRepository;
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

            // Skip if already exists
            if (typeRepository.findById((long) typeId).isPresent()) {
                log.debug("Type #{} already exists, skipping", typeId);
                return null;
            }

            PokeApiTypeResponse typeData = pokeApiClient.getType(typeId);
            if (typeData == null) {
                log.warn("No data received for Type #{}", typeId);
                return null;
            }

            Type type = mapToType(typeData);
            Type savedType = typeRepository.save(type);

            // Add translations
            addTranslations(savedType, typeData);
            typeRepository.save(savedType);

            log.info("Successfully imported Type #{}: {}", typeId, type.getIdentifier());
            return savedType;

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

        // Set generation based on when type was introduced
        // Most types are from Gen 1, special cases handled separately
        // Generation relationship would need to be set if we have Generation entities loaded

        return type;
    }

    private void addTranslations(Type savedType, PokeApiTypeResponse typeData) {
        if (typeData.getNames() == null) {
            // Add at least English translation
            TypeTranslation englishTranslation = TypeTranslation.builder()
                    .type(savedType)
                    .language(Language.EN)
                    .name(capitalizeFirst(typeData.getName()))
                    .build();

            savedType.setTranslations(List.of(englishTranslation));
            return;
        }

        List<TypeTranslation> translations = new ArrayList<>();

        // English translation (default)
        TypeTranslation englishTranslation = TypeTranslation.builder()
                .type(savedType)
                .language(Language.EN)
                .name(capitalizeFirst(typeData.getName()))
                .build();
        translations.add(englishTranslation);

        // Add other language translations
        for (PokeApiName name : typeData.getNames()) {
            Language language = Util.mapLanguage(name.getLanguage().getName());
            if (language != null && language != Language.EN) {
                TypeTranslation translation = TypeTranslation.builder()
                        .type(savedType)
                        .language(language)
                        .name(name.getName())
                        .build();

                translations.add(translation);
            }
        }

        savedType.setTranslations(translations);
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