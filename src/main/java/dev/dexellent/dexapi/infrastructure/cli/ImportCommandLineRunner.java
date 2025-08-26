package dev.dexellent.dexapi.infrastructure.cli;

import dev.dexellent.dexapi.application.service.ImportService;
import dev.dexellent.dexapi.infrastructure.importer.ImportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImportCommandLineRunner implements CommandLineRunner {

    private final ImportService importService;

    @Override
    public void run(String... args) throws Exception {
        // Only run if specific argument is passed
        boolean runImport = false;
        for (String arg : args) {
            if ("--run-import".equals(arg) || "--import".equals(arg)) {
                runImport = true;
                break;
            }
        }

        if (!runImport) {
            return;
        }

        log.info("=== DexAPI Data Import Tool ===");

        Scanner scanner = new Scanner(System.in);

        try {
            // Show available importers
            var availableImporters = importService.getAvailableImporters();
            var healthyImporters = importService.getHealthyImporterNames();

            System.out.println("\nAvailable import sources:");
            int index = 1;
            for (String sourceName : availableImporters.keySet()) {
                boolean isHealthy = healthyImporters.contains(sourceName);
                System.out.printf("%d. %s %s\n", index++, sourceName,
                        isHealthy ? "(✓ healthy)" : "(✗ unhealthy)");
            }

            if (healthyImporters.isEmpty()) {
                System.out.println("\nNo healthy import sources available. Exiting...");
                return;
            }

            // Select source
            System.out.print("\nSelect import source (enter number): ");
            int sourceIndex = scanner.nextInt() - 1;

            String[] sourceNames = availableImporters.keySet().toArray(new String[0]);
            if (sourceIndex < 0 || sourceIndex >= sourceNames.length) {
                System.out.println("Invalid selection. Exiting...");
                return;
            }

            String selectedSource = sourceNames[sourceIndex];
            if (!healthyImporters.contains(selectedSource)) {
                System.out.println("Selected source is not healthy. Exiting...");
                return;
            }

            // Get limit
            System.out.print("Number of Pokemon to import (1-1025): ");
            int limit = scanner.nextInt();

            if (limit < 1 || limit > 1025) {
                System.out.println("Invalid limit. Must be between 1 and 1000. Exiting...");
                return;
            }

            // Get starting offset
            System.out.print("Starting ID (default 0): ");
            int offset = scanner.nextInt();

            if (offset < 0) {
                offset = 0;
            }

            // Confirm
            System.out.printf("\nImport Configuration:\n");
            System.out.printf("- Source: %s\n", selectedSource);
            System.out.printf("- Count: %d Pokemon\n", limit);
            System.out.printf("- Starting from ID: %d\n", offset + 1);
            System.out.print("\nProceed with import? (y/N): ");

            String confirm = scanner.next().toLowerCase();
            if (!"y".equals(confirm) && !"yes".equals(confirm)) {
                System.out.println("Import cancelled.");
                return;
            }

            // Execute import
            System.out.println("\nStarting import...");

            int batchSize = Math.min(20, limit); // Import in batches of 20 or less
            ImportResult result = importService.importPokemonBatch(selectedSource, limit, batchSize);

            // Display results
            System.out.println("\n=== Import Results ===");
            System.out.printf("Source: %s\n", result.getSource());
            System.out.printf("Status: %s\n", result.isSuccess() ? "SUCCESS" : "FAILED");
            System.out.printf("Total Records: %d\n", result.getTotalRecords());
            System.out.printf("Successful: %d\n", result.getSuccessfulImports());
            System.out.printf("Failed: %d\n", result.getFailedImports());
            System.out.printf("Duration: %d ms\n", result.getDurationMs());

            if (!result.getErrors().isEmpty()) {
                System.out.println("\nErrors:");
                result.getErrors().forEach(error -> System.out.println("- " + error));
            }

            System.out.println("\nImport completed!");

        } catch (Exception e) {
            log.error("Import command failed", e);
            System.out.println("Import failed: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}