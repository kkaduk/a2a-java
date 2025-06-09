// src/main/java/net/kaduk/a2a/A2AAutoConfiguration.java
package net.kaduk.a2a;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configuration for the a2a-java library.
 * Ensures all components in 'net.kaduk.a2a' are registered when used as a dependency.
 */
@Configuration
@ComponentScan(basePackages = "net.kaduk.a2a", excludeFilters = {
    @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, 
                         value = {A2AAgentRegistry.class, A2AWebClientService.class, AgentController.class})
})
public class A2AAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public A2AWebClientService a2aWebClientService(WebClient webClient) {
        return new A2AWebClientService(webClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public A2AAgentRegistry a2aAgentRegistry() {
        return new A2AAgentRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn({"a2aAgentRegistry", "a2aWebClientService"})
    public AgentController agentController(A2AAgentRegistry a2aAgentRegistry) {
        return new AgentController(a2aAgentRegistry);
    }
}