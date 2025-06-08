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
public class MessageSendConfiguration {

    @JsonProperty("acceptedOutputModes")
    private List<String> acceptedOutputModes;

    @JsonProperty("blocking")
    private Boolean blocking;

    @JsonProperty("historyLength")
    private Integer historyLength;

    // For now, omitting the pushNotificationConfig for brevity; add as needed.
}
