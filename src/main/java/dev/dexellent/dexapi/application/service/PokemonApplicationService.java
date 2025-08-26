package dev.dexellent.dexapi.application.service;

import dev.dexellent.dexapi.domain.model.Pokemon;
import dev.dexellent.dexapi.domain.model.PokemonTranslation;
import dev.dexellent.dexapi.domain.model.enums.Language;
import dev.dexellent.dexapi.domain.repository.PokemonRepository;
import dev.dexellent.dexapi.domain.service.LanguageService;
import dev.dexellent.dexapi.infrastructure.web.dto.response.PokemonResponse;
import dev.dexellent.dexapi.infrastructure.web.mapper.PokemonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PokemonApplicationService {

    private final PokemonRepository pokemonRepository;
    private final LanguageService languageService;
    private final PokemonMapper pokemonMapper;

    @Cacheable(value = "pokemon", key = "#id + '_' + #languageCode")
    public PokemonResponse findById(Long id, String languageCode) {
        Language language = languageService.getLanguage(languageCode);

        Pokemon pokemon = pokemonRepository.findByIdWithTranslations(id, language)
                .orElseThrow(() -> new PokemonNotFoundException("Pokemon not found with id: " + id));

        log.debug("Found Pokemon with id: {} in language: {}", id, language.getCode());
        return pokemonMapper.toResponse(pokemon, language);
    }

    @Cacheable(value = "pokemon", key = "#identifier + '_' + #languageCode")
    public PokemonResponse findByIdentifier(String identifier, String languageCode) {
        Language language = languageService.getLanguage(languageCode);

        Pokemon pokemon = pokemonRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new PokemonNotFoundException("Pokemon not found with identifier: " + identifier));

        return pokemonMapper.toResponse(pokemon, language);
    }

    public PokemonResponse findByName(String name, String languageCode) {
        Language language = languageService.getLanguage(languageCode);

        Pokemon pokemon = pokemonRepository.findByNameInLanguage(name, language)
                .orElseThrow(() -> new PokemonNotFoundException("Pokemon not found with name: " + name + " in language: " + language.getCode()));

        return pokemonMapper.toResponse(pokemon, language);
    }

    @Cacheable(value = "pokemon_list", key = "#languageCode + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PokemonResponse> findAll(String languageCode, Pageable pageable) {
        Language language = languageService.getLanguage(languageCode);

        Page<Pokemon> pokemonPage = pokemonRepository.findAllWithTranslations(language, pageable);
        return pokemonPage.map(pokemon -> pokemonMapper.toResponse(pokemon, language));
    }

    public Page<PokemonResponse> searchByName(String name, String languageCode, Pageable pageable) {
        Language language = languageService.getLanguage(languageCode);

        Page<Pokemon> pokemonPage = pokemonRepository.findByNameContainingInLanguage(name, language, pageable);
        return pokemonPage.map(pokemon -> pokemonMapper.toResponse(pokemon, language));
    }

    public Page<PokemonResponse> findByGeneration(Long generationId, String languageCode, Pageable pageable) {
        Language language = languageService.getLanguage(languageCode);

        Page<Pokemon> pokemonPage = pokemonRepository.findByGenerationId(generationId, language, pageable);
        return pokemonPage.map(pokemon -> pokemonMapper.toResponse(pokemon, language));
    }

    public List<PokemonResponse> findByIds(List<Long> ids, String languageCode) {
        Language language = languageService.getLanguage(languageCode);

        List<Pokemon> pokemon = pokemonRepository.findByIds(ids, language);
        return pokemon.stream()
                .map(p -> pokemonMapper.toResponse(p, language))
                .toList();
    }

    public List<String> getAvailableLanguages(Long pokemonId) {
        List<Language> languages = pokemonRepository.findAvailableLanguages(pokemonId);
        return languages.stream()
                .map(Language::getCode)
                .toList();
    }
}