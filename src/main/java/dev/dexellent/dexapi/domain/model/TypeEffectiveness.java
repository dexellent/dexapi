package dev.dexellent.dexapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "type_effectiveness")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeEffectiveness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attacking_type_id", nullable = false)
    private Type attackingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defending_type_id", nullable = false)
    private Type defendingType;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal effectiveness; // 0.0, 0.25, 0.5, 1.0, 2.0, 4.0
}
