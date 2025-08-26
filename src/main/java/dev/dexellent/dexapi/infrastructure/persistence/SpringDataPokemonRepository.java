package dev.dexellent.dexapi.infrastructure.persistence;

import dev.dexellent.dexapi.domain.model.Pokemon;
import dev.dexellent.dexapi.domain.model.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface SpringDataPokemonRepository extends JpaRepository<Pokemon, Long> {

    @Query("""
        SELECT DISTINCT p FROM Pokemon p 
        LEFT JOIN FETCH p.translations t 
        LEFT JOIN FETCH p.types pt 
        LEFT JOIN FETCH pt.type 
        WHERE p.id = :id 
        AND (t.language = :language OR t.language = 'EN' OR t IS NULL)
        """)
    Optional<Pokemon> findByIdWithTranslations(@Param("id") Long id,
                                               @Param("language") Language language);

    @Query("""
        SELECT DISTINCT p FROM Pokemon p 
        LEFT JOIN FETCH p.translations t 
        WHERE p.nationalDexNumber = :nationalDexNumber
        """)
    Optional<Pokemon> findByNationalDexNumber(@Param("nationalDexNumber") Integer nationalDexNumber);

    @Query("""
        SELECT DISTINCT p FROM Pokemon p 
        LEFT JOIN FETCH p.translations t 
        WHERE p.identifier = :identifier
        """)
    Optional<Pokemon> findByIdentifier(@Param("identifier") String identifier);

    @Query("""
        SELECT DISTINCT p FROM Pokemon p 
        JOIN p.translations t 
        WHERE LOWER(t.name) = LOWER(:name) 
        AND t.language = :language
        """)
    Optional<Pokemon> findByNameInLanguage(@Param("name") String name,
                                           @Param("language") Language language);

    @Query(value = """
        SELECT DISTINCT p FROM Pokemon p 
        LEFT JOIN FETCH p.translations t 
        WHERE (t.language = :language OR t.language = 'EN' OR t IS NULL)
        """,
            countQuery = "SELECT COUNT(DISTINCT p) FROM Pokemon p")
    Page<Pokemon> findAllWithTranslations(@Param("language") Language language, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT p FROM Pokemon p 
        JOIN p.translations t 
        WHERE t.language = :language 
        AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p) FROM Pokemon p 
        JOIN p.translations t 
        WHERE t.language = :language 
        AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
        """)
    Page<Pokemon> findByNameContainingInLanguage(@Param("name") String name,
                                                 @Param("language") Language language,
                                                 Pageable pageable);

    @Query(value = """
        SELECT DISTINCT p FROM Pokemon p 
        LEFT JOIN FETCH p.translations t 
        WHERE p.generation.id = :generationId 
        AND (t.language = :language OR t.language = 'EN' OR t IS NULL)
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p) FROM Pokemon p 
        WHERE p.generation.id = :generationId
        """)
    Page<Pokemon> findByGenerationId(@Param("generationId") Long generationId,
                                     @Param("language") Language language,
                                     Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Pokemon p 
        LEFT JOIN FETCH p.translations t 
        WHERE p.id IN :ids 
        AND (t.language = :language OR t.language = 'EN' OR t IS NULL)
        """)
    List<Pokemon> findByIds(@Param("ids") List<Long> ids, @Param("language") Language language);

    boolean existsByIdentifier(String identifier);

    @Query("""
        SELECT DISTINCT t.language FROM Pokemon p 
        JOIN p.translations t 
        WHERE p.id = :pokemonId
        """)
    List<Language> findAvailableLanguages(@Param("pokemonId") Long pokemonId);
}
