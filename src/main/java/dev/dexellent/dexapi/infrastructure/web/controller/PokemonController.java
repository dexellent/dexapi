package dev.dexellent.dexapi.infrastructure.web.controller;

import dev.dexellent.dexapi.application.service.PokemonApplicationService;
import dev.dexellent.dexapi.domain.service.LanguageService;
import dev.dexellent.dexapi.infrastructure.web.dto.response.ApiResponse;
import dev.dexellent.dexapi.infrastructure.web.dto.response.PokemonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pokemon")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pokemon", description = "Pokemon information endpoints with multilingual support")
public class PokemonController {

    private final PokemonApplicationService pokemonService;
    private final LanguageService languageService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Pokemon by ID",
            description = "Retrieve detailed information about a specific Pokemon by its ID. " +
                    "Supports multiple languages via the 'lang' parameter or Accept-Language header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pokemon found successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Pokemon not found"
            )
    })
    public ResponseEntity<ApiResponse<PokemonResponse>> getPokemonById(
            @Parameter(description = "Pokemon ID", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Language code (en, fr, ja, es, de)", example = "en")
            @RequestParam(required = false) String lang,

            HttpServletRequest request) {

        String languageCode = determineLanguage(lang, request);
        log.info("Fetching Pokemon with ID: {} in language: {}", id, languageCode);

        PokemonResponse pokemon = pokemonService.findById(id, languageCode);

        // Add available languages to the response
        List<String> availableLanguages = pokemonService.getAvailableLanguages(id);
        pokemon.setAvailableLanguages(availableLanguages);

        return ResponseEntity.ok(ApiResponse.<PokemonResponse>builder()
                .success(true)
                .data(pokemon)
                .meta(Map.of(
                        "language", languageCode,
                        "available_languages", availableLanguages
                ))
                .build());
    }

    @GetMapping("/identifier/{identifier}")
    @Operation(
            summary = "Get Pokemon by identifier",
            description = "Retrieve Pokemon information using its identifier (e.g., 'pikachu')"
    )
    public ResponseEntity<ApiResponse<PokemonResponse>> getPokemonByIdentifier(
            @Parameter(description = "Pokemon identifier", example = "pikachu")
            @PathVariable String identifier,

            @Parameter(description = "Language code", example = "en")
            @RequestParam(required = false) String lang,

            HttpServletRequest request) {

        String languageCode = determineLanguage(lang, request);
        log.info("Fetching Pokemon with identifier: {} in language: {}", identifier, languageCode);

        PokemonResponse pokemon = pokemonService.findByIdentifier(identifier, languageCode);

        List<String> availableLanguages = pokemonService.getAvailableLanguages(pokemon.getId());
        pokemon.setAvailableLanguages(availableLanguages);

        return ResponseEntity.ok(ApiResponse.<PokemonResponse>builder()
                .success(true)
                .data(pokemon)
                .meta(Map.of(
                        "language", languageCode,
                        "available_languages", availableLanguages
                ))
                .build());
    }

    @GetMapping("/name/{name}")
    @Operation(
            summary = "Get Pokemon by translated name",
            description = "Find Pokemon by its name in the specified language"
    )
    public ResponseEntity<ApiResponse<PokemonResponse>> getPokemonByName(
            @Parameter(description = "Pokemon name in specified language", example = "Pikachu")
            @PathVariable String name,

            @Parameter(description = "Language code", example = "en")
            @RequestParam(required = false) String lang,

            HttpServletRequest request) {

        String languageCode = determineLanguage(lang, request);
        log.info("Fetching Pokemon with name: {} in language: {}", name, languageCode);

        PokemonResponse pokemon = pokemonService.findByName(name, languageCode);

        List<String> availableLanguages = pokemonService.getAvailableLanguages(pokemon.getId());
        pokemon.setAvailableLanguages(availableLanguages);

        return ResponseEntity.ok(ApiResponse.<PokemonResponse>builder()
                .success(true)
                .data(pokemon)
                .meta(Map.of(
                        "language", languageCode,
                        "available_languages", availableLanguages
                ))
                .build());
    }

    @GetMapping
    @Operation(
            summary = "Search Pokemon",
            description = "Search and filter Pokemon with pagination support. " +
                    "Supports searching by name, filtering by generation, and sorting options."
    )
    public ResponseEntity<ApiResponse<Page<PokemonResponse>>> searchPokemon(
            @Parameter(description = "Search by Pokemon name", example = "pika")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by generation ID", example = "1")
            @RequestParam(required = false) Long generationId,

            @Parameter(description = "Language code", example = "en")
            @RequestParam(required = false) String lang,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (max 100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field", example = "nationalDexNumber")
            @RequestParam(defaultValue = "nationalDexNumber") String sort,

            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String direction,

            HttpServletRequest request) {

        String languageCode = determineLanguage(lang, request);
        log.info("Searching Pokemon - name: {}, generation: {}, language: {}, page: {}, size: {}",
                name, generationId, languageCode, page, size);

        // Create sort object
        Sort sortObj = Sort.by(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort
        );
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<PokemonResponse> results;

        if (generationId != null) {
            results = pokemonService.findByGeneration(generationId, languageCode, pageable);
        } else if (name != null && !name.isBlank()) {
            results = pokemonService.searchByName(name.trim(), languageCode, pageable);
        } else {
            results = pokemonService.findAll(languageCode, pageable);
        }

        return ResponseEntity.ok(ApiResponse.<Page<PokemonResponse>>builder()
                .success(true)
                .data(results)
                .meta(Map.of(
                        "language", languageCode,
                        "total_elements", results.getTotalElements(),
                        "total_pages", results.getTotalPages(),
                        "current_page", results.getNumber(),
                        "page_size", results.getSize(),
                        "has_next", results.hasNext(),
                        "has_previous", results.hasPrevious()
                ))
                .build());
    }

    @PostMapping("/bulk")
    @Operation(
            summary = "Get multiple Pokemon by IDs",
            description = "Retrieve multiple Pokemon at once by providing a list of IDs"
    )
    public ResponseEntity<ApiResponse<List<PokemonResponse>>> getBulkPokemon(
            @Parameter(description = "List of Pokemon IDs")
            @RequestBody List<Long> ids,

            @Parameter(description = "Language code")
            @RequestParam(required = false) String lang,

            HttpServletRequest request) {

        String languageCode = determineLanguage(lang, request);
        log.info("Fetching bulk Pokemon - IDs: {}, language: {}", ids, languageCode);

        List<PokemonResponse> pokemon = pokemonService.findByIds(ids, languageCode);

        return ResponseEntity.ok(ApiResponse.<List<PokemonResponse>>builder()
                .success(true)
                .data(pokemon)
                .meta(Map.of(
                        "language", languageCode,
                        "requested_count", ids.size(),
                        "returned_count", pokemon.size()
                ))
                .build());
    }

    @GetMapping("/languages")
    @Operation(
            summary = "Get supported languages",
            description = "Retrieve list of all supported languages for the API"
    )
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getSupportedLanguages() {

        List<Map<String, String>> languages = languageService.getSupportedLanguages().stream()
                .map(lang -> Map.of(
                        "code", lang.getCode(),
                        "name", lang.getEnglishName(),
                        "native_name", lang.getNativeName()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<Map<String, String>>>builder()
                .success(true)
                .data(languages)
                .meta(Map.of(
                        "default_language", languageService.getDefaultLanguage().getCode(),
                        "total_languages", languages.size()
                ))
                .build());
    }

    private String determineLanguage(String langParam, HttpServletRequest request) {
        return langParam != null ? langParam :
                languageService.detectLanguage(request).getCode();
    }
}