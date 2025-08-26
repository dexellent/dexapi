package dev.dexellent.dexapi.infrastructure.persistence;

import dev.dexellent.dexapi.domain.model.Type;
import dev.dexellent.dexapi.domain.model.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface SpringDataTypeRepository extends JpaRepository<Type, Long> {
    Optional<Type> findByIdentifier(String identifier);

    @Query("""
        SELECT DISTINCT t FROM Type t 
        JOIN t.translations tt 
        WHERE LOWER(tt.name) = LOWER(:name) 
        AND tt.language = :language
        """)
    Optional<Type> findByNameInLanguage(@Param("name") String name, @Param("language") Language language);

    boolean existsByIdentifier(String identifier);
    List<Type> findByOrderByIdAsc();

    @Query("""
        SELECT DISTINCT tt.language FROM Type t 
        JOIN t.translations tt 
        WHERE t.id = :typeId
        """)
    List<Language> findAvailableLanguages(@Param("typeId") Long typeId);
}