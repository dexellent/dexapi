package dev.dexellent.dexapi.application.service;

import dev.dexellent.dexapi.domain.model.Pokemon;
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

    private final List<DataImporter<Pokemon>> importers;

    public Map<String, DataImporter<Pokemon>> getAvailableImporters() {
        return importers.stream()
                .collect(Collectors.toMap(
                        DataImporter::getSourceName,
                        importer -> importer
                ));
    }

    public List<String> getHealthyImporterNames() {
        return importers.stream()
                .filter(DataImporter::isHealthy)
                .map(DataImporter::getSourceName)
                .collect(Collectors.toList());
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

        if (!importer.isHealthy()) {
            return ImportResult.builder()
                    .success(false)
                    .errors(List.of("Import source is not healthy: " + sourceName))
                    .source(sourceName)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .build();
        }

        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting import from {} - limit: {}, offset: {}", sourceName, limit, offset);

        try {
            List<Pokemon> importedData = importer.importData(limit, offset);
            LocalDateTime endTime = LocalDateTime.now();

            ImportResult validationResult = importer.validateData(importedData);

            return ImportResult.builder()
                    .success(validationResult.isSuccess())
                    .totalRecords(importedData.size())
                    .successfulImports(importedData.size())
                    .failedImports(0)
                    .errors(validationResult.getErrors())
                    .source(sourceName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

        } catch (Exception e) {
            log.error("Import failed for source {}: {}", sourceName, e.getMessage(), e);

            return ImportResult.builder()
                    .success(false)
                    .totalRecords(0)
                    .successfulImports(0)
                    .failedImports(1)
                    .errors(List.of("Import failed: " + e.getMessage()))
                    .source(sourceName)
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .build();
        }
    }

    public ImportResult importPokemonBatch(String sourceName, int totalLimit, int batchSize) {
        List<String> allErrors = List.of();
        int totalSuccessful = 0;
        int totalFailed = 0;
        LocalDateTime overallStart = LocalDateTime.now();

        for (int offset = 0; offset < totalLimit; offset += batchSize) {
            int currentBatch = Math.min(batchSize, totalLimit - offset);
            log.info("Processing batch: offset={}, size={}", offset, currentBatch);

            ImportResult batchResult = importPokemon(sourceName, currentBatch, offset);

            totalSuccessful += batchResult.getSuccessfulImports();
            totalFailed += batchResult.getFailedImports();

            if (!batchResult.getErrors().isEmpty()) {
                allErrors = List.copyOf(allErrors);
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