// src/main/java/net/kaduk/a2a/CapabilityDiscoveryResponse.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CapabilityDiscoveryResponse {
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("agentCount")
    private Integer agentCount;
    
    @JsonProperty("agents")
    private List<AgentCapabilityInfo> agents;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
}