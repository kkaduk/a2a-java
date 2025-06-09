// src/main/java/net/kaduk/a2a/CancelTaskResponse.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelTaskResponse {
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";

    @JsonProperty("id")
    private String id;

    @JsonProperty("result")
    private Task result;

    @JsonProperty("error")
    private A2AError error;
}