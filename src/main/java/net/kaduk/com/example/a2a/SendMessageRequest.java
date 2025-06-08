package com.example.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessageRequest {
    @Builder.Default
    @JsonProperty("jsonrpc")
    private final String jsonrpc = "2.0";

    @Builder.Default
    @JsonProperty("method")
    private final String method = "message/send";

    @JsonProperty("id")
    private String id;

    @JsonProperty("params")
    private MessageSendParams params;
}
