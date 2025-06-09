// src/main/java/net/kaduk/a2a/TaskIdParams.java
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
public class TaskIdParams {
    @JsonProperty("id")
    private String id;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}