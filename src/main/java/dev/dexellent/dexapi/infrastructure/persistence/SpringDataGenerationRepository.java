package dev.dexellent.dexapi.infrastructure.persistence;

import dev.dexellent.dexapi.domain.model.Generation;

import java.util.List;
import java.util.Optional;

interface SpringDataGenerationRepository extends org.springframework.data.jpa.repository.JpaRepository<Generation, Long> {
    Optional<Generation> findByNumber(Integer number);

    Optional<Generation> findByName(String name);

    boolean existsByNumber(Integer number);

    List<Generation> findByOrderByNumberAsc();
}