package dev.dexellent.dexapi.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypeResponse {
    private Long id;
    private String identifier;
    private String name;
    private String color;
    private Integer slot; // For Pokemon types (1 = primary, 2 = secondary)
}