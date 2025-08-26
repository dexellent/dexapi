package dev.dexellent.dexapi.infrastructure.web.controller.admin;

import dev.dexellent.dexapi.application.service.ImportService;
import dev.dexellent.dexapi.infrastructure.importer.ImportResult;
import dev.dexellent.dexapi.infrastructure.importer.config.ImportConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/admin/import")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "dexapi.import.enable-web-interface", havingValue = "true", matchIfMissing = true)
public class ImportController {

    private final ImportService importService;
    private final ImportConfig importConfig;
    private final Map<String, ImportResult> activeImports = new ConcurrentHashMap<>();

    @GetMapping
    public String importPage(Model model) {
        model.addAttribute("importers", importService.getAvailableImporters().keySet());
        model.addAttribute("healthyImporters", importService.getHealthyImporterNames());
        model.addAttribute("config", importConfig);
        return "import/index";
    }

    @PostMapping("/start/full")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startFullImport(
            @RequestParam int pokemonLimit,
            @RequestParam(defaultValue = "20") int batchSize) {

        Map<String, Object> response = new HashMap<>();

        if (pokemonLimit < 1 || pokemonLimit > 1025) {
            response.put("success", false);
            response.put("error", "Pokemon limit must be between 1 and 1025");
            return ResponseEntity.badRequest().body(response);
        }

        String importId = generateImportId();

        // Start full import asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting full import: {} pokemon", pokemonLimit);

                // Step 1: Import Generations
                log.info("Step 1: Importing generations");
                ImportResult genResult = importService.importGenerations(9, 0);
                if (!genResult.isSuccess()) {
                    throw new RuntimeException("Generation import failed: " + String.join(", ", genResult.getErrors()));
                }

                // Step 2: Import Types
                log.info("Step 2: Importing types");
                ImportResult typeResult = importService.importTypes(18, 0);
                if (!typeResult.isSuccess()) {
                    throw new RuntimeException("Type import failed: " + String.join(", ", typeResult.getErrors()));
                }

                // Step 3: Import Pokemon
                log.info("Step 3: Importing {} pokemon", pokemonLimit);
                ImportResult pokemonResult = importService.importPokemonBatch("PokeAPI v2 - Pokemon", pokemonLimit, batchSize);

                // Combine results
                return ImportResult.builder()
                        .success(pokemonResult.isSuccess())
                        .totalRecords(genResult.getSuccessfulImports() + typeResult.getSuccessfulImports() + pokemonResult.getTotalRecords())
                        .successfulImports(genResult.getSuccessfulImports() + typeResult.getSuccessfulImports() + pokemonResult.getSuccessfulImports())
                        .failedImports(pokemonResult.getFailedImports())
                        .errors(pokemonResult.getErrors())
                        .source("Full Import (Gen + Types + Pokemon)")
                        .startTime(genResult.getStartTime())
                        .endTime(pokemonResult.getEndTime())
                        .build();

            } catch (Exception e) {
                log.error("Full import failed", e);
                return ImportResult.builder()
                        .success(false)
                        .errors(java.util.List.of("Full import failed: " + e.getMessage()))
                        .source("Full Import")
                        .build();
            }
        }).thenAccept(result -> {
            activeImports.put(importId, result);
            log.info("Full import {} completed: {} successful, {} failed",
                    importId, result.getSuccessfulImports(), result.getFailedImports());
        });

        response.put("success", true);
        response.put("importId", importId);
        response.put("message", "Full import started successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/start/generations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startGenerationImport() {
        String importId = generateImportId();

        CompletableFuture.supplyAsync(() -> {
            try {
                return importService.importGenerations(9, 0);
            } catch (Exception e) {
                log.error("Generation import failed", e);
                return ImportResult.builder()
                        .success(false)
                        .errors(java.util.List.of("Generation import failed: " + e.getMessage()))
                        .source("PokeAPI v2 - Generations")
                        .build();
            }
        }).thenAccept(result -> activeImports.put(importId, result));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("importId", importId);
        response.put("message", "Generation import started successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/start/types")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startTypeImport() {
        String importId = generateImportId();

        CompletableFuture.supplyAsync(() -> {
            try {
                return importService.importTypes(18, 0);
            } catch (Exception e) {
                log.error("Type import failed", e);
                return ImportResult.builder()
                        .success(false)
                        .errors(java.util.List.of("Type import failed: " + e.getMessage()))
                        .source("PokeAPI v2 - Types")
                        .build();
            }
        }).thenAccept(result -> activeImports.put(importId, result));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("importId", importId);
        response.put("message", "Type import started successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startImport(
            @RequestParam String source,
            @RequestParam int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int batchSize) {

        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (limit < 1 || limit > 1025) {
            response.put("success", false);
            response.put("error", "Limit must be between 1 and 1025");
            return ResponseEntity.badRequest().body(response);
        }

        if (offset < 0) {
            response.put("success", false);
            response.put("error", "Offset cannot be negative");
            return ResponseEntity.badRequest().body(response);
        }

        if (!importService.getAvailableImporters().containsKey(source)) {
            response.put("success", false);
            response.put("error", "Unknown import source: " + source);
            return ResponseEntity.badRequest().body(response);
        }

        if (!importService.getHealthyImporterNames().contains(source)) {
            response.put("success", false);
            response.put("error", "Import source is not healthy: " + source);
            return ResponseEntity.badRequest().body(response);
        }

        String importId = generateImportId();

        // Start import asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                return importService.importPokemonBatch(source, limit, batchSize);
            } catch (Exception e) {
                log.error("Async import failed", e);
                return ImportResult.builder()
                        .success(false)
                        .errors(java.util.List.of("Import failed: " + e.getMessage()))
                        .source(source)
                        .build();
            }
        }).thenAccept(result -> {
            activeImports.put(importId, result);
            log.info("Import {} completed: {} successful, {} failed",
                    importId, result.getSuccessfulImports(), result.getFailedImports());
        });

        response.put("success", true);
        response.put("importId", importId);
        response.put("message", "Import started successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{importId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getImportStatus(@PathVariable String importId) {
        Map<String, Object> response = new HashMap<>();

        ImportResult result = activeImports.get(importId);
        if (result == null) {
            response.put("status", "running");
            response.put("message", "Import in progress...");
        } else {
            response.put("status", "completed");
            response.put("result", result);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getImporterHealth() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Boolean> healthStatus = new HashMap<>();

        importService.getAvailableImporters().forEach((name, importer) -> {
            healthStatus.put(name, importer.isHealthy());
        });

        response.put("importers", healthStatus);
        response.put("healthy", importService.getHealthyImporterNames());

        return ResponseEntity.ok(response);
    }

    private String generateImportId() {
        return "import_" + System.currentTimeMillis() + "_" +
                Integer.toHexString((int)(Math.random() * 0x1000));
    }
}
