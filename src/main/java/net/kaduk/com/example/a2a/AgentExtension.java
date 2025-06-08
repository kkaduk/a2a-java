package com.example.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentExtension {
    @JsonProperty("description")
    private String description;

    @JsonProperty("params")
    private Map<String, Object> params;

    @JsonProperty("required")
    private Boolean required;

    @JsonProperty("uri")
    private String uri;
}
