// src/main/java/net/kaduk/a2a/AgentCapabilityInfo.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Information about an agent's capabilities.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentCapabilityInfo {
    @JsonProperty("agentName")
    private String agentName;
    
    @JsonProperty("agentVersion")
    private String agentVersion;
    
    @JsonProperty("agentUrl")
    private String agentUrl;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("availableSkills")
    private List<SkillCapability> availableSkills;
    
    @JsonProperty("confidence")
    private Double confidence;
}