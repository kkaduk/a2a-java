// src/main/java/net/kaduk/a2a/SendStreamingMessageRequest.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendStreamingMessageRequest {
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";

    @JsonProperty("method")
    @Builder.Default
    private String method = "message/stream";

    @JsonProperty("id")
    private String id;

    @JsonProperty("params")
    private MessageSendParams params;
}