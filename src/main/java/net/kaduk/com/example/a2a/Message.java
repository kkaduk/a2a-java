package com.example.a2a;

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
public class Message {
    @JsonProperty("kind")
    private String kind;

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("parts")
    private List<Part> parts;

    @JsonProperty("role")
    private String role; // "agent" or "user"

    @JsonProperty("contextId")
    private String contextId;

    @JsonProperty("extensions")
    private List<String> extensions;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("referenceTaskIds")
    private List<String> referenceTaskIds;

    @JsonProperty("taskId")
    private String taskId;
}
