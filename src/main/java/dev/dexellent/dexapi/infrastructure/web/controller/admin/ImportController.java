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

    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startImport(
            @RequestParam String source,
            @RequestParam int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int batchSize) {

        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (limit < 1 || limit > 1000) {
            response.put("success", false);
            response.put("error", "Limit must be between 1 and 1000");
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