package net.kaduk.a2a;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ComponentScan(basePackages = "net.kaduk.a2a")
@EntityScan(basePackages = "net.kaduk.a2a")
@EnableJpaRepositories(basePackages = "net.kaduk.a2a")
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
    @DependsOn("agentRepository")
    public A2AAgentRegistry a2aAgentRegistry(AgentRepository agentRepository) {
        return new A2AAgentRegistry(agentRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("a2aAgentRegistry")
    public AgentController agentController(A2AAgentRegistry a2aAgentRegistry) {
        return new AgentController(a2aAgentRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public Receptionist receptionist(AgentRepository agentRepository,
            A2AAgentRegistry agentRegistry,
            A2AWebClientService webClientService) {
        return new Receptionist(agentRepository, agentRegistry, webClientService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReceptionistController receptionistController(Receptionist receptionist) {
        return new ReceptionistController(receptionist);
    }
}