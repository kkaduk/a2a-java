// src/main/java/net/kaduk/a2a/A2AWebClientConfiguration.java
package net.kaduk.a2a;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class A2AWebClientConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}