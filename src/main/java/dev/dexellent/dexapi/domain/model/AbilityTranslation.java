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
@Table(name = "ability_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ability_id", "language"}),
        indexes = {
                @Index(name = "idx_ability_translations_ability_lang", columnList = "ability_id, language"),
                @Index(name = "idx_ability_translations_name", columnList = "name")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ability_id", nullable = false)
    private Ability ability;

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String effect;

    @Column(columnDefinition = "TEXT")
    private String shortEffect;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}