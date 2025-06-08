package com.example.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentSkill {
    @JsonProperty("description")
    private String description;

    @JsonProperty("examples")
    private List<String> examples;

    @JsonProperty("id")
    private String id;

    @JsonProperty("inputModes")
    private List<String> inputModes;

    @JsonProperty("name")
    private String name;

    @JsonProperty("outputModes")
    private List<String> outputModes;

    @JsonProperty("tags")
    private List<String> tags;
}
