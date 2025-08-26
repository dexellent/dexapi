package dev.dexellent.dexapi.domain.model;

import dev.dexellent.dexapi.domain.model.enums.EvolutionTrigger;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evolutions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_pokemon_id", nullable = false)
    private Pokemon fromPokemon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_pokemon_id", nullable = false)
    private Pokemon toPokemon;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private EvolutionTrigger trigger; // LEVEL_UP, TRADE, STONE, etc.

    @Column
    private Integer minimumLevel;

    @Column(length = 100)
    private String item;

    @Column(length = 100)
    private String condition;

    @Column
    private Integer minimumHappiness;

    @Column(length = 50)
    private String timeOfDay;

    @Column(length = 100)
    private String location;

    @Column
    private Integer order; // For branching evolutions
}
