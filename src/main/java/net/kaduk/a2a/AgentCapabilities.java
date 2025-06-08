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
public class AgentCapabilities {
    @JsonProperty("extensions")
    private List<AgentExtension> extensions;

    @JsonProperty("pushNotifications")
    private Boolean pushNotifications;

    @JsonProperty("stateTransitionHistory")
    private Boolean stateTransitionHistory;

    @JsonProperty("streaming")
    private Boolean streaming;
}
