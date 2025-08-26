package dev.dexellent.dexapi.infrastructure.importer.pokeapi.dto;

import lombok.Data;

@Data
public class PokeApiNamedResource {
    private String name;
    private String url;

    public Long extractId() {
        if (url == null) return null;
        String[] parts = url.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty()) {
                try {
                    return Long.parseLong(parts[i]);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return null;
    }
}