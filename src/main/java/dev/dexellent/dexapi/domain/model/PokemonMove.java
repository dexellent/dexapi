package dev.dexellent.dexapi.domain.model;

import dev.dexellent.dexapi.domain.model.enums.LearnMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pokemon_moves")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PokemonMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "move_id", nullable = false)
    private Move move;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LearnMethod learnMethod; // LEVEL_UP, TM, EGG, TUTOR

    @Column
    private Integer levelLearned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id")
    private Generation generation;
}
