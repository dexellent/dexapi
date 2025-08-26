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
@Table(name = "type_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"type_id", "language"}),
        indexes = {
                @Index(name = "idx_type_translations_type_lang", columnList = "type_id, language"),
                @Index(name = "idx_type_translations_name", columnList = "name")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private Type type;

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(nullable = false, length = 50)
    private String name;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
