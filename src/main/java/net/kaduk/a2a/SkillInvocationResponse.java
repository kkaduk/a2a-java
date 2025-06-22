// src/main/java/net/kaduk/a2a/SkillInvocationResponse.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response from a skill invocation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillInvocationResponse {
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("result")
    private Message result;
    
    @JsonProperty("taskId")
    private String taskId;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
}