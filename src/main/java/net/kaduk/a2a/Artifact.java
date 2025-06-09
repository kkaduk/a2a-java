// src/main/java/net/kaduk/a2a/Artifact.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Artifact {
    @JsonProperty("artifactId")
    private String artifactId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("extensions")
    private List<String> extensions;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("name")
    private String name;

    @JsonProperty("parts")
    private List<Part> parts;
}