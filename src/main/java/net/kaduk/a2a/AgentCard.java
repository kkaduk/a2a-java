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
public class AgentCard {
    @JsonProperty("capabilities")
    private AgentCapabilities capabilities;

    @JsonProperty("defaultInputModes")
    private List<String> defaultInputModes;

    @JsonProperty("defaultOutputModes")
    private List<String> defaultOutputModes;

    @JsonProperty("description")
    private String description;

    @JsonProperty("documentationUrl")
    private String documentationUrl;

    @JsonProperty("iconUrl")
    private String iconUrl;

    @JsonProperty("name")
    private String name;

    @JsonProperty("provider")
    private AgentProvider provider;

    @JsonProperty("security")
    private List<Map<String, List<String>>> security;

    @JsonProperty("securitySchemes")
    private Map<String, Object> securitySchemes;

    @JsonProperty("skills")
    private List<AgentSkill> skills;

    @JsonProperty("supportsAuthenticatedExtendedCard")
    private Boolean supportsAuthenticatedExtendedCard;

    @JsonProperty("url")
    private String url;

    @JsonProperty("version")
    private String version;
}
