package dev.dexellent.dexapi.infrastructure.persistence;

import dev.dexellent.dexapi.domain.model.Generation;
import dev.dexellent.dexapi.domain.repository.GenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaGenerationRepository implements GenerationRepository {

    private final SpringDataGenerationRepository springDataRepository;

    @Override
    public Optional<Generation> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Generation> findByNumber(Integer number) {
        return springDataRepository.findByNumber(number);
    }

    @Override
    public Optional<Generation> findByName(String name) {
        return springDataRepository.findByName(name);
    }

    @Override
    public Page<Generation> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public List<Generation> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public Generation save(Generation generation) {
        return springDataRepository.save(generation);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public boolean existsByNumber(Integer number) {
        return springDataRepository.existsByNumber(number);
    }

    @Override
    public List<Generation> findByOrderByNumberAsc() {
        return springDataRepository.findByOrderByNumberAsc();
    }
}
