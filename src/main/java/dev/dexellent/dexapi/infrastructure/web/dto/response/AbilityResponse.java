package dev.dexellent.dexapi.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbilityResponse {
    private Long id;
    private String identifier;
    private String name;
    private String description;
    private String effect;

    @JsonProperty("short_effect")
    private String shortEffect;

    @JsonProperty("is_hidden")
    private Boolean isHidden;

    private Integer slot;
}
