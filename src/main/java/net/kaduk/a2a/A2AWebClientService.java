package net.kaduk.a2a;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for agent-to-agent (A2A) JSON-RPC communication over HTTP using WebFlux.
 */
public class A2AWebClientService {

    private final WebClient webClient;

    public A2AWebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Sends an A2A JSON-RPC message/send request to the agent at the provided URL.
     * @param agentUrl the agent endpoint
     * @param request SendMessageRequest object
     * @return Mono of String (JSON-RPC response as string; replace with DTO as needed)
     */
    public Mono<String> sendMessage(String agentUrl, SendMessageRequest request) {
        return webClient.post()
                .uri(agentUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class); // Could use bodyToMono(CustomResponse.class)
    }
}
