package dev.dexellent.dexapi.infrastructure.importer.pokeapi;

import dev.dexellent.dexapi.domain.model.enums.Language;

public class Util {

    public static Language mapLanguage(String pokeApiLanguage) {
        return switch (pokeApiLanguage) {
            case "en" -> Language.EN;
            case "fr" -> Language.FR;
            case "ja", "ja-Hrkt" -> Language.JA;
            case "es" -> Language.ES;
            case "de" -> Language.DE;
            case "it" -> Language.IT;
            case "ko" -> Language.KO;
            case "zh", "zh-Hant", "zh-Hans" -> Language.ZH;
            default -> null;
        };
    }
}
