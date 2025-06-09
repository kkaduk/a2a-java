// src/main/java/net/kaduk/a2a/Task.java
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
public class Task {
    @JsonProperty("artifacts")
    private List<Artifact> artifacts;

    @JsonProperty("contextId")
    private String contextId;

    @JsonProperty("history")
    private List<Message> history;

    @JsonProperty("id")
    private String id;

    @JsonProperty("kind")
    @Builder.Default
    private String kind = "task";

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("status")
    private TaskStatus status;
}