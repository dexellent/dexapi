package dev.dexellent.dexapi.infrastructure.importer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ImportResult {
    private boolean success;
    private int totalRecords;
    private int successfulImports;
    private int failedImports;
    private List<String> errors;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String source;

    public long getDurationMs() {
        if (startTime == null || endTime == null) return 0;
        return java.time.Duration.between(startTime, endTime).toMillis();
    }
}