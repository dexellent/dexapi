package dev.dexellent.dexapi.infrastructure.web.service;

import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.service.LanguageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class LanguageServiceImpl implements LanguageService {

    private static final Language DEFAULT_LANGUAGE = Language.EN;
    private static final List<Language> SUPPORTED_LANGUAGES = Arrays.asList(
            Language.EN, Language.FR, Language.JA, Language.ES, Language.DE
    );

    @Override
    public Language detectLanguage(HttpServletRequest request) {
        // 1. Check query parameter first (highest priority)
        String langParam = request.getParameter("lang");
        if (langParam != null && isLanguageSupported(langParam)) {
            return Language.fromCode(langParam);
        }

        // 2. Check Accept-Language header
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
            // Parse Accept-Language header (e.g., "fr-FR,fr;q=0.9,en;q=0.8")
            String[] languages = acceptLanguage.split(",");
            for (String lang : languages) {
                String langCode = lang.split(";")[0].trim();
                if (langCode.contains("-")) {
                    langCode = langCode.split("-")[0]; // Extract primary language
                }
                if (isLanguageSupported(langCode)) {
                    return Language.fromCode(langCode);
                }
            }
        }

        return DEFAULT_LANGUAGE;
    }

    @Override
    public Language getLanguage(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return DEFAULT_LANGUAGE;
        }

        Language language = Language.fromCode(languageCode);
        return isLanguageSupported(language.getCode()) ? language : DEFAULT_LANGUAGE;
    }

    @Override
    public Language getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @Override
    public List<Language> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    @Override
    public boolean isLanguageSupported(String languageCode) {
        if (languageCode == null) return false;
        return SUPPORTED_LANGUAGES.stream()
                .anyMatch(lang -> lang.getCode().equalsIgnoreCase(languageCode));
    }
}