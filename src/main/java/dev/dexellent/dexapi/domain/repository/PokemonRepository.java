package dev.dexellent.dexapi.domain.repository;

import dev.dexellent.dexapi.domain.model.Pokemon;
import dev.dexellent.dexapi.domain.model.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PokemonRepository {
    Optional<Pokemon> findById(Long id);

    Optional<Pokemon> findByIdWithTranslations(Long id, Language language);

    Optional<Pokemon> findByNationalDexNumber(Integer nationalDexNumber);

    Optional<Pokemon> findByIdentifier(String identifier);

    Optional<Pokemon> findByNameInLanguage(String name, Language language);

    Page<Pokemon> findAll(Pageable pageable);

    Page<Pokemon> findAllWithTranslations(Language language, Pageable pageable);

    Page<Pokemon> findByNameContainingInLanguage(String name, Language language, Pageable pageable);

    Page<Pokemon> findByGenerationId(Long generationId, Language language, Pageable pageable);

    List<Pokemon> findByIds(List<Long> ids, Language language);

    Pokemon save(Pokemon pokemon);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByIdentifier(String identifier);

    List<Language> findAvailableLanguages(Long pokemonId);
}
