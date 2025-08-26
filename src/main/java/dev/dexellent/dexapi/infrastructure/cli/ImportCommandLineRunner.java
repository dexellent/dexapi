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
            System.out.println("1. Full Import (Generations → Types → Pokemon)");
            System.out.println("2. Generations only");
            System.out.println("3. Types only");
            System.out.println("4. Pokemon only");

            System.out.println("\nHealthy importers:");
            healthyImporters.forEach(name -> System.out.println("- " + name + " ✓"));

            if (healthyImporters.isEmpty()) {
                System.out.println("\nNo healthy import sources available. Exiting...");
                return;
            }

            // Select import type
            System.out.print("\nSelect import type (1-4): ");
            int importType = scanner.nextInt();

            switch (importType) {
                case 1 -> runFullImport(scanner);
                case 2 -> runGenerationImport(scanner);
                case 3 -> runTypeImport(scanner);
                case 4 -> runPokemonImport(scanner);
                default -> {
                    System.out.println("Invalid selection. Exiting...");
                    return;
                }
            }

        } catch (Exception e) {
            log.error("Import command failed", e);
            System.out.println("Import failed: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private void runFullImport(Scanner scanner) {
        System.out.println("\n=== Full Import Process ===");
        System.out.println("This will import in order: Generations → Types → Pokemon");
        System.out.print("Number of Pokemon to import (1-1025): ");
        int pokemonLimit = scanner.nextInt();

        System.out.print("Proceed with full import? (y/N): ");
        String confirm = scanner.next().toLowerCase();
        if (!"y".equals(confirm) && !"yes".equals(confirm)) {
            System.out.println("Import cancelled.");
            return;
        }

        System.out.println("\n=== Step 1: Importing Generations ===");
        ImportResult generationResult = importService.importGenerations(9, 0); // All 9 generations
        displayResults(generationResult);

        if (!generationResult.isSuccess()) {
            System.out.println("Generation import failed. Stopping...");
            return;
        }

        System.out.println("\n=== Step 2: Importing Types ===");
        ImportResult typeResult = importService.importTypes(18, 0); // All 18 types
        displayResults(typeResult);

        if (!typeResult.isSuccess()) {
            System.out.println("Type import failed. Stopping...");
            return;
        }

        System.out.println("\n=== Step 3: Importing Pokemon ===");
        ImportResult pokemonResult = importService.importPokemonBatch("PokeAPI v2 - Pokemon", pokemonLimit, 20);
        displayResults(pokemonResult);

        System.out.println("\n=== Full Import Complete! ===");
        System.out.printf("Total imported: %d generations, %d types, %d pokemon\n",
                generationResult.getSuccessfulImports(),
                typeResult.getSuccessfulImports(),
                pokemonResult.getSuccessfulImports());
    }

    private void runGenerationImport(Scanner scanner) {
        System.out.println("\n=== Generation Import ===");
        System.out.print("Import all 9 generations? (y/N): ");
        String confirm = scanner.next().toLowerCase();

        if (!"y".equals(confirm) && !"yes".equals(confirm)) {
            System.out.println("Import cancelled.");
            return;
        }

        ImportResult result = importService.importGenerations(9, 0);
        displayResults(result);
    }

    private void runTypeImport(Scanner scanner) {
        System.out.println("\n=== Type Import ===");
        System.out.print("Import all 18 types? (y/N): ");
        String confirm = scanner.next().toLowerCase();

        if (!"y".equals(confirm) && !"yes".equals(confirm)) {
            System.out.println("Import cancelled.");
            return;
        }

        ImportResult result = importService.importTypes(18, 0);
        displayResults(result);
    }

    private void runPokemonImport(Scanner scanner) {
        System.out.println("\n=== Pokemon Import ===");
        System.out.print("Number of Pokemon to import (1-1025): ");
        int limit = scanner.nextInt();

        if (limit < 1 || limit > 1025) {
            System.out.println("Invalid limit. Must be between 1 and 1025. Exiting...");
            return;
        }

        System.out.print("Starting ID (default 0): ");
        int offset = scanner.nextInt();

        if (offset < 0) {
            offset = 0;
        }

        System.out.printf("\nWill import %d Pokemon starting from ID %d\n", limit, offset + 1);
        System.out.print("Proceed? (y/N): ");
        String confirm = scanner.next().toLowerCase();

        if (!"y".equals(confirm) && !"yes".equals(confirm)) {
            System.out.println("Import cancelled.");
            return;
        }

        ImportResult result = importService.importPokemonBatch("PokeAPI v2 - Pokemon", limit, 20);
        displayResults(result);
    }

    private void displayResults(ImportResult result) {
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
    }
}
