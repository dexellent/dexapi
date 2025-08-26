package dev.dexellent.dexapi.domain.repository;

import dev.dexellent.dexapi.domain.model.Generation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GenerationRepository {
    Optional<Generation> findById(Long id);

    Optional<Generation> findByNumber(Integer number);

    Optional<Generation> findByName(String name);

    Page<Generation> findAll(Pageable pageable);

    List<Generation> findAll();

    Generation save(Generation generation);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByNumber(Integer number);

    List<Generation> findByOrderByNumberAsc();
}
