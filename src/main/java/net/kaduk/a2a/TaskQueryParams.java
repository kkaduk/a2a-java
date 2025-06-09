// src/main/java/net/kaduk/a2a/TaskQueryParams.java
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
public class TaskQueryParams {
    @JsonProperty("historyLength")
    private Integer historyLength;

    @JsonProperty("id")
    private String id;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}