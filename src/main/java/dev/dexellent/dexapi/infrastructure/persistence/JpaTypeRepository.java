package dev.dexellent.dexapi.infrastructure.persistence;

import dev.dexellent.dexapi.domain.model.Type;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaTypeRepository implements TypeRepository {

    private final SpringDataTypeRepository springDataRepository;

    @Override
    public Optional<Type> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Type> findByIdentifier(String identifier) {
        return springDataRepository.findByIdentifier(identifier);
    }

    @Override
    public Optional<Type> findByNameInLanguage(String name, Language language) {
        return springDataRepository.findByNameInLanguage(name, language);
    }

    @Override
    public Page<Type> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public List<Type> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public Type save(Type type) {
        return springDataRepository.save(type);
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
    public boolean existsByIdentifier(String identifier) {
        return springDataRepository.existsByIdentifier(identifier);
    }

    @Override
    public List<Type> findByOrderByIdAsc() {
        return springDataRepository.findByOrderByIdAsc();
    }

    @Override
    public List<Language> findAvailableLanguages(Long typeId) {
        return springDataRepository.findAvailableLanguages(typeId);
    }
}
