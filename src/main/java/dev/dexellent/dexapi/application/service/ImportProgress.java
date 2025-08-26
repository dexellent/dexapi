package dev.dexellent.dexapi.application.service;

import dev.dexellent.dexapi.infrastructure.importer.ImportStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ImportProgress {
    private String importId;
    private ImportStatus status;
    private String source;
    private int totalCount;
    private AtomicInteger processedCount;
    private AtomicInteger successCount;
    private AtomicInteger errorCount;
    private LocalDateTime startTime;
    private LocalDateTime lastUpdate;
    private String currentOperation;
    private java.util.List<String> recentLogs;

    public ImportProgress(String importId, String source, int totalCount) {
        this.importId = importId;
        this.source = source;
        this.totalCount = totalCount;
        this.processedCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.errorCount = new AtomicInteger(0);
        this.status = ImportStatus.PENDING;
        this.startTime = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
        this.recentLogs = new java.util.ArrayList<>();
    }

    public void addLog(String message) {
        recentLogs.add(LocalDateTime.now() + ": " + message);
        if (recentLogs.size() > 50) { // Keep only last 50 logs
            recentLogs.remove(0);
        }
        this.lastUpdate = LocalDateTime.now();
    }

    public double getProgressPercentage() {
        if (totalCount == 0) return 0;
        return (double) processedCount.get() / totalCount * 100;
    }
}