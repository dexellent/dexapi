package dev.dexellent.dexapi.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Language {
    EN("en", "English", "English"),
    FR("fr", "Français", "French"),
    JA("ja", "日本語", "Japanese"),
    ES("es", "Español", "Spanish"),
    DE("de", "Deutsch", "German"),
    IT("it", "Italiano", "Italian"),
    KO("ko", "한국어", "Korean"),
    ZH("zh", "中文", "Chinese");

    private final String code;
    private final String nativeName;
    private final String englishName;

    public static Language fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return EN;
        }

        return Arrays.stream(values())
                .filter(lang -> lang.code.equalsIgnoreCase(code.trim()) ||
                        lang.code.equalsIgnoreCase(code.substring(0, Math.min(2, code.length()))))
                .findFirst()
                .orElse(EN);
    }

    public static Language fromCodeStrict(String code) {
        return Arrays.stream(values())
                .filter(lang -> lang.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language code: " + code));
    }
}
