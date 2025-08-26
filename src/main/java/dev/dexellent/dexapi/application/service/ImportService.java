package dev.dexellent.dexapi.application.service;

import dev.dexellent.dexapi.domain.model.Generation;
import dev.dexellent.dexapi.domain.model.Pokemon;
import dev.dexellent.dexapi.domain.model.Type;
import dev.dexellent.dexapi.infrastructure.importer.DataImporter;
import dev.dexellent.dexapi.infrastructure.importer.ImportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final List<DataImporter<Pokemon>> pokemonImporters;
    private final List<DataImporter<Type>> typeImporters;
    private final List<DataImporter<Generation>> generationImporters;

    public Map<String, DataImporter<Pokemon>> getAvailableImporters() {
        return pokemonImporters.stream()
                .collect(Collectors.toMap(
                        DataImporter::getSourceName,
                        importer -> importer
                ));
    }

    public List<String> getHealthyImporterNames() {
        List<String> healthy = pokemonImporters.stream()
                .filter(DataImporter::isHealthy)
                .map(DataImporter::getSourceName)
                .collect(Collectors.toList());

        // Add type and generation importers
        typeImporters.stream()
                .filter(DataImporter::isHealthy)
                .map(DataImporter::getSourceName)
                .forEach(healthy::add);

        generationImporters.stream()
                .filter(DataImporter::isHealthy)
                .map(DataImporter::getSourceName)
                .forEach(healthy::add);

        return healthy;
    }

    @Transactional
    public ImportResult importGenerations(int limit, int offset) {
        DataImporter<Generation> importer = generationImporters.stream()
                .filter(imp -> imp.getSourceName().contains("Generation"))
                .findFirst()
                .orElse(null);

        if (importer == null) {
            return ImportResult.builder()
                    .success(false)
                    .errors(List.of("No Generation importer available"))
                    .source("Generation Import")
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .build();
        }

        return executeImport(importer, limit, offset, "Generations");
    }

    @Transactional
    public ImportResult importTypes(int limit, int offset) {
        DataImporter<Type> importer = typeImporters.stream()
                .filter(imp -> imp.getSourceName().contains("Type"))
                .findFirst()
                .orElse(null);

        if (importer == null) {
            return ImportResult.builder()
                    .success(false)
                    .errors(List.of("No Type importer available"))
                    .source("Type Import")
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .build();
        }

        return executeImport(importer, limit, offset, "Types");
    }

    private <T> ImportResult executeImport(DataImporter<T> importer, int limit, int offset, String entityType) {
        if (!importer.isHealthy()) {
            return ImportResult.builder()
                    .success(false)
                    .errors(List.of(entityType + " importer is not healthy"))
                    .source(importer.getSourceName())
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .build();
        }

        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting {} import - limit: {}, offset: {}", entityType, limit, offset);

        try {
            List<T> importedData = importer.importData(limit, offset);
            LocalDateTime endTime = LocalDateTime.now();

            ImportResult validationResult = importer.validateData(importedData);

            return ImportResult.builder()
                    .success(validationResult.isSuccess())
                    .totalRecords(importedData.size())
                    .successfulImports(importedData.size())
                    .failedImports(0)
                    .errors(validationResult.getErrors())
                    .source(importer.getSourceName())
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

        } catch (Exception e) {
            log.error("{} import failed: {}", entityType, e.getMessage(), e);

            return ImportResult.builder()
                    .success(false)
                    .totalRecords(0)
                    .successfulImports(0)
                    .failedImports(1)
                    .errors(List.of(entityType + " import failed: " + e.getMessage()))
                    .source(importer.getSourceName())
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .build();
        }
    }

    @Transactional
    public ImportResult importPokemon(String sourceName, int limit, int offset) {
        DataImporter<Pokemon> importer = getAvailableImporters().get(sourceName);
        if (importer == null) {
            return ImportResult.builder()
                    .success(false)
                    .errors(List.of("Unknown import source: " + sourceName))
                    .source(sourceName)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .build();
        }

        return executeImport(importer, limit, offset, "Pokemon");
    }

    public ImportResult importPokemonBatch(String sourceName, int totalLimit, int batchSize) {
        List<String> allErrors = new java.util.ArrayList<>();
        int totalSuccessful = 0;
        int totalFailed = 0;
        LocalDateTime overallStart = LocalDateTime.now();

        for (int offset = 0; offset < totalLimit; offset += batchSize) {
            int currentBatch = Math.min(batchSize, totalLimit - offset);
            log.info("Processing Pokemon batch: offset={}, size={}", offset, currentBatch);

            ImportResult batchResult = importPokemon(sourceName, currentBatch, offset);

            totalSuccessful += batchResult.getSuccessfulImports();
            totalFailed += batchResult.getFailedImports();

            if (!batchResult.getErrors().isEmpty()) {
                allErrors.addAll(batchResult.getErrors());
            }

            // Add delay between batches to be respectful to APIs
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return ImportResult.builder()
                .success(totalFailed == 0)
                .totalRecords(totalSuccessful + totalFailed)
                .successfulImports(totalSuccessful)
                .failedImports(totalFailed)
                .errors(allErrors)
                .source(sourceName)
                .startTime(overallStart)
                .endTime(LocalDateTime.now())
                .build();
    }
}