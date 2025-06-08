package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageSendParams {
    @JsonProperty("message")
    private Message message;

    @JsonProperty("configuration")
    private Object configuration; // Omit for now

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
