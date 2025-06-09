// src/main/java/net/kaduk/a2a/TaskPushNotificationConfig.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskPushNotificationConfig {
    @JsonProperty("pushNotificationConfig")
    private PushNotificationConfig pushNotificationConfig;

    @JsonProperty("taskId")
    private String taskId;
}