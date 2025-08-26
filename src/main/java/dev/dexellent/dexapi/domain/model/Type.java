package dev.dexellent.dexapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import dev.dexellent.dexapi.domain.model.enums.Language;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String identifier; // e.g., "electric"

    @Column(length = 7) // For hex color codes
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id")
    private Generation generation;

    // Translations
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TypeTranslation> translations;

    @OneToMany(mappedBy = "attackingType", cascade = CascadeType.ALL)
    private List<TypeEffectiveness> attackingEffectiveness;

    @OneToMany(mappedBy = "defendingType", cascade = CascadeType.ALL)
    private List<TypeEffectiveness> defendingEffectiveness;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for translations
    public Optional<TypeTranslation> getTranslation(Language language) {
        if (translations == null) return Optional.empty();
        return translations.stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst();
    }

    public TypeTranslation getTranslationOrDefault(Language language) {
        return getTranslation(language)
                .orElse(getTranslation(Language.EN)
                        .orElse(null));
    }
}
