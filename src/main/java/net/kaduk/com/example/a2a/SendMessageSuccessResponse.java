package com.example.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessageSuccessResponse {
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";

    @JsonProperty("id")
    private String id;

    @JsonProperty("result")
    private Message result;
}
