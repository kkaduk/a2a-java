// src/main/java/net/kaduk/a2a/BestAgentResponse.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BestAgentResponse {
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("agent")
    private AgentCapabilityInfo agent;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
}