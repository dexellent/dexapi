package dev.dexellent.dexapi.domain.service;

import dev.dexellent.dexapi.domain.model.enums.Language;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface LanguageService {
    Language detectLanguage(HttpServletRequest request);
    Language getLanguage(String languageCode);
    Language getDefaultLanguage();
    List<Language> getSupportedLanguages();
    boolean isLanguageSupported(String languageCode);
}
