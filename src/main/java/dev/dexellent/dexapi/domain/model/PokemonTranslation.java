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

@Entity
@Table(name = "pokemon_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pokemon_id", "language"}),
        indexes = {
                @Index(name = "idx_pokemon_translations_pokemon_lang", columnList = "pokemon_id, language"),
                @Index(name = "idx_pokemon_translations_name", columnList = "name"),
                @Index(name = "idx_pokemon_translations_name_lower", columnList = "LOWER(name)")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PokemonTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String species; // e.g., "Mouse Pokémon"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String habitat;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}