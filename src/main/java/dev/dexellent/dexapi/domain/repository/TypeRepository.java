package dev.dexellent.dexapi.domain.repository;

import dev.dexellent.dexapi.domain.model.Type;
import dev.dexellent.dexapi.domain.model.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TypeRepository {
    Optional<Type> findById(Long id);
    Optional<Type> findByIdentifier(String identifier);
    Optional<Type> findByNameInLanguage(String name, Language language);

    Page<Type> findAll(Pageable pageable);
    List<Type> findAll();

    Type save(Type type);
    void deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByIdentifier(String identifier);

    List<Type> findByOrderByIdAsc();
    List<Language> findAvailableLanguages(Long typeId);
}