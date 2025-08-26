package dev.dexellent.dexapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.model.enums.MoveCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "moves")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String identifier; // e.g., "thunderbolt"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private Type type;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MoveCategory category;

    @Column
    private Integer power;

    @Column
    private Integer accuracy;

    @Column(nullable = false)
    private Integer powerPoints;

    @Column
    private Integer priority;

    @Column(length = 50)
    private String target;

    @Column
    private Integer criticalHitRate;

    @Column
    private Integer flinchChance;

    @Column
    private Integer statChange;

    @Column(length = 50)
    private String ailment;

    @Column
    private Integer ailmentChance;

    @Column
    private Integer healing;

    @Column
    private Integer drain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id")
    private Generation generation;

    // Translations
    @OneToMany(mappedBy = "move", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MoveTranslation> translations;

    @OneToMany(mappedBy = "move", cascade = CascadeType.ALL)
    private List<PokemonMove> pokemonMoves;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for translations
    public Optional<MoveTranslation> getTranslation(Language language) {
        if (translations == null) return Optional.empty();
        return translations.stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst();
    }

    public MoveTranslation getTranslationOrDefault(Language language) {
        return getTranslation(language)
                .orElse(getTranslation(Language.EN)
                        .orElse(null));
    }
}