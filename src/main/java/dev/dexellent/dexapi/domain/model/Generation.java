package dev.dexellent.dexapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "generations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Generation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer number;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String region;

    @Column
    private Integer releaseYear;

    @ElementCollection
    @CollectionTable(name = "generation_games", joinColumns = @JoinColumn(name = "generation_id"))
    @Column(name = "game_name")
    private List<String> games;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL)
    private List<Pokemon> pokemon;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL)
    private List<Move> moves;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL)
    private List<Ability> abilities;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL)
    private List<Type> types;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
