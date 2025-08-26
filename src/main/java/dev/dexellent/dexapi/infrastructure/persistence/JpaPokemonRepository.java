package dev.dexellent.dexapi.infrastructure.persistence;

import dev.dexellent.dexapi.domain.model.Pokemon;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.repository.PokemonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPokemonRepository implements PokemonRepository {

    private final SpringDataPokemonRepository springDataRepository;

    @Override
    public Optional<Pokemon> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Pokemon> findByIdWithTranslations(Long id, Language language) {
        return springDataRepository.findByIdWithTranslations(id, language);
    }

    @Override
    public Optional<Pokemon> findByNationalDexNumber(Integer nationalDexNumber) {
        return springDataRepository.findByNationalDexNumber(nationalDexNumber);
    }

    @Override
    public Optional<Pokemon> findByIdentifier(String identifier) {
        return springDataRepository.findByIdentifier(identifier);
    }

    @Override
    public Optional<Pokemon> findByNameInLanguage(String name, Language language) {
        return springDataRepository.findByNameInLanguage(name, language);
    }

    @Override
    public Page<Pokemon> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public Page<Pokemon> findAllWithTranslations(Language language, Pageable pageable) {
        return springDataRepository.findAllWithTranslations(language, pageable);
    }

    @Override
    public Page<Pokemon> findByNameContainingInLanguage(String name, Language language, Pageable pageable) {
        return springDataRepository.findByNameContainingInLanguage(name, language, pageable);
    }

    @Override
    public Page<Pokemon> findByGenerationId(Long generationId, Language language, Pageable pageable) {
        return springDataRepository.findByGenerationId(generationId, language, pageable);
    }

    @Override
    public List<Pokemon> findByIds(List<Long> ids, Language language) {
        return springDataRepository.findByIds(ids, language);
    }

    @Override
    public Pokemon save(Pokemon pokemon) {
        return springDataRepository.save(pokemon);
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
    public List<Language> findAvailableLanguages(Long pokemonId) {
        return springDataRepository.findAvailableLanguages(pokemonId);
    }
}