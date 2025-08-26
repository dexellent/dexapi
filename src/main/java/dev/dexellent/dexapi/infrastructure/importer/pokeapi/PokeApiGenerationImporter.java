package dev.dexellent.dexapi.infrastructure.importer.pokeapi;

import dev.dexellent.dexapi.domain.model.Generation;
import dev.dexellent.dexapi.domain.repository.GenerationRepository;
import dev.dexellent.dexapi.infrastructure.importer.DataImporter;
import dev.dexellent.dexapi.infrastructure.importer.ImportResult;
import dev.dexellent.dexapi.infrastructure.importer.config.ImportConfig;
import dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto.PokeApiGenerationResponse;
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
public class PokeApiGenerationImporter implements DataImporter<Generation> {

    private final PokeApiClient pokeApiClient;
    private final GenerationRepository generationRepository;
    private final ImportConfig importConfig;

    @Override
    public String getSourceName() {
        return "PokeAPI v2 - Generations";
    }

    @Override
    public List<Generation> importData(int limit, int offset) {
        List<Generation> importedGenerations = new ArrayList<>();

        // Generations in PokeAPI are numbered 1-9 currently
        int maxGenerations = Math.min(limit, 9);

        for (int i = offset + 1; i <= offset + maxGenerations; i++) {
            if (i > 9) break; // Don't exceed known generations

            try {
                Generation imported = importSingleGeneration(i);
                if (imported != null) {
                    importedGenerations.add(imported);
                }

                Thread.sleep(150); // Rate limiting

            } catch (Exception e) {
                log.error("Failed to import Generation #{}: {}", i, e.getMessage());
            }
        }

        return importedGenerations;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Generation importSingleGeneration(int generationId) {
        try {
            log.debug("Importing Generation #{}", generationId);

            // Skip if already exists
            if (generationRepository.findByNumber(generationId).isPresent()) {
                log.debug("Generation #{} already exists, skipping", generationId);
                return null;
            }

            PokeApiGenerationResponse generationData = pokeApiClient.getGeneration(generationId);
            if (generationData == null) {
                log.warn("No data received for Generation #{}", generationId);
                return null;
            }

            Generation generation = mapToGeneration(generationData);
            Generation savedGeneration = generationRepository.save(generation);

            log.info("Successfully imported Generation #{}: {}", generationId, generation.getName());
            return savedGeneration;

        } catch (Exception e) {
            log.error("Failed to import Generation #{}: {}", generationId, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isHealthy() {
        return importConfig.getPokeApi().isEnabled() && pokeApiClient.isHealthy();
    }

    @Override
    public ImportResult validateData(List<Generation> data) {
        List<String> errors = new ArrayList<>();
        int validCount = 0;

        for (Generation generation : data) {
            if (generation.getNumber() == null || generation.getNumber() <= 0) {
                errors.add("Invalid generation number for: " + generation.getName());
                continue;
            }

            if (generation.getName() == null || generation.getName().trim().isEmpty()) {
                errors.add("Missing name for Generation #" + generation.getNumber());
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

    private Generation mapToGeneration(PokeApiGenerationResponse generationData) {
        Generation generation = Generation.builder()
                .number(generationData.getId().intValue())
                .name(capitalizeFirst(generationData.getName().replace("-", " ")))
                .build();

        // Extract region name from main_region if available
        if (generationData.getMainRegion() != null) {
            generation.setRegion(capitalizeFirst(generationData.getMainRegion().getName()));
        }

        // Extract games from version_groups
        if (generationData.getVersionGroups() != null) {
            List<String> games = generationData.getVersionGroups().stream()
                    .map(vg -> capitalizeFirst(vg.getName().replace("-", " ")))
                    .toList();
            generation.setGames(games);
        }

        // Set approximate release years based on generation
        generation.setReleaseYear(getApproximateReleaseYear(generation.getNumber()));

        return generation;
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private Integer getApproximateReleaseYear(Integer genNumber) {
        return switch (genNumber) {
            case 1 -> 1996;
            case 2 -> 1999;
            case 3 -> 2002;
            case 4 -> 2006;
            case 5 -> 2010;
            case 6 -> 2013;
            case 7 -> 2016;
            case 8 -> 2019;
            case 9 -> 2022;
            default -> null;
        };
    }
}