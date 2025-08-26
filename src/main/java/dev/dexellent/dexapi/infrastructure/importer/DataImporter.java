package dev.dexellent.dexapi.infrastructure.importer;

import java.util.List;

public interface DataImporter<T> {
    String getSourceName();

    List<T> importData(int limit, int offset);

    boolean isHealthy();

    ImportResult validateData(List<T> data);
}
