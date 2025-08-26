package dev.dexellent.dexapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import dev.dexellent.dexapi.domain.model.enums.Language;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "pokemon")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer nationalDexNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String identifier; // e.g., "pikachu" - used for API calls and fallback

    // Stats
    @Column(nullable = false)
    private Integer hp;

    @Column(nullable = false)
    private Integer attack;

    @Column(nullable = false)
    private Integer defense;

    @Column(nullable = false)
    private Integer specialAttack;

    @Column(nullable = false)
    private Integer specialDefense;

    @Column(nullable = false)
    private Integer speed;

    // Physical characteristics
    @Column(precision = 5, scale = 2)
    private BigDecimal height; // in meters

    @Column(precision = 6, scale = 3)
    private BigDecimal weight; // in kg

    @Column
    private Integer captureRate;

    @Column
    private Integer baseExperience;

    @Column(length = 50)
    private String growthRate;

    @Column(length = 50)
    private String genderRatio;

    @Column
    private Integer eggCycles;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String shape;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id")
    private Generation generation;

    // Translations
    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PokemonTranslation> translations;

    // Other relationships
    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PokemonType> types;

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PokemonAbility> abilities;

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PokemonMove> moves;

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PokedexEntry> pokedexEntries;

    @OneToMany(mappedBy = "fromPokemon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evolution> evolutionsFrom;

    @OneToMany(mappedBy = "toPokemon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evolution> evolutionsTo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for translations
    public Optional<PokemonTranslation> getTranslation(Language language) {
        if (translations == null) return Optional.empty();
        return translations.stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst();
    }

    public PokemonTranslation getTranslationOrDefault(Language language) {
        return getTranslation(language)
                .orElse(getTranslation(Language.EN)
                        .orElse(null));
    }
}