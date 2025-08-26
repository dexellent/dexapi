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
@Table(name = "move_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"move_id", "language"}),
        indexes = {
                @Index(name = "idx_move_translations_move_lang", columnList = "move_id, language"),
                @Index(name = "idx_move_translations_name", columnList = "name")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "move_id", nullable = false)
    private Move move;

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String effect;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}