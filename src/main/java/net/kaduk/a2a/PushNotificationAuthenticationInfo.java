// src/main/java/net/kaduk/a2a/PushNotificationAuthenticationInfo.java
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
public class PushNotificationAuthenticationInfo {
    @JsonProperty("credentials")
    private String credentials;

    @JsonProperty("schemes")
    private List<String> schemes;
}