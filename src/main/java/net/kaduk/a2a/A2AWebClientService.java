// Delete the incorrectly named file and create the correct one
// src/main/java/net/kaduk/a2a/A2AWebClientService.java
package net.kaduk.a2a;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for agent-to-agent (A2A) JSON-RPC communication over HTTP using
 * WebFlux.
 */
@Component
@Slf4j
public class A2AWebClientService {

    private final WebClient webClient;

    public A2AWebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Sends an A2A JSON-RPC message/send request to the agent at the provided URL.
     */
    public Mono<SendMessageSuccessResponse> sendMessage(String agentUrl, SendMessageRequest request) {
        log.info("(KK) Sending message to webclient at URL: " + agentUrl);
        log.info("(KK) Request body: " + request.toString());
        var xxx = webClient.post()
                .uri(agentUrl + "/agent/message")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SendMessageSuccessResponse.class);
        log.info("(KK) Response from webclient: " + xxx.subscribe().toString());
        return xxx;
    }

    /**
     * Sends a streaming message request.
     */
    public Flux<Object> sendStreamingMessage(String agentUrl, SendStreamingMessageRequest request) {
        return webClient.post()
                .uri(agentUrl + "/agent/streaming")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(Object.class);
    }

    /**
     * Gets a task by ID.
     */
    public Mono<GetTaskResponse> getTask(String agentUrl, GetTaskRequest request) {
        return webClient.post()
                .uri(agentUrl + "/agent/task")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GetTaskResponse.class);
    }

    /**
     * Cancels a task.
     */
    public Mono<CancelTaskResponse> cancelTask(String agentUrl, CancelTaskRequest request) {
        return webClient.post()
                .uri(agentUrl + "/agent/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CancelTaskResponse.class);
    }
}