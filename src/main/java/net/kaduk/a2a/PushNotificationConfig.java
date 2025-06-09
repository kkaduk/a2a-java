// src/main/java/net/kaduk/a2a/PushNotificationConfig.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushNotificationConfig {
    @JsonProperty("id")
    private String id;

    @JsonProperty("token")
    private String token;

    @JsonProperty("url")
    private String url;

    @JsonProperty("authentication")
    private PushNotificationAuthenticationInfo authentication;
}