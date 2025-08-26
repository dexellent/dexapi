package dev.dexellent.dexapi.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerationResponse {
    private Long id;
    private Integer number;
    private String name;
    private String region;

    @JsonProperty("release_year")
    private Integer releaseYear;

    private List<String> games;
}
