package com.example.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataPart extends Part {
    @Builder.Default
    @JsonProperty("kind")
    private final String kind = "data";

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
