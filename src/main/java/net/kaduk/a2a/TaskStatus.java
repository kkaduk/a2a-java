// src/main/java/net/kaduk/a2a/TaskStatus.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskStatus {
    @JsonProperty("message")
    private Message message;

    @JsonProperty("state")
    private TaskState state;

    @JsonProperty("timestamp")
    private String timestamp;
}