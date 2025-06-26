// src/main/java/net/kaduk/a2a/AgentController.java
package net.kaduk.a2a;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Lazy // Important: Lazy initialization to avoid circular dependency
@Slf4j
public class AgentController {

        private final A2AAgentRegistry agentRegistry;

        // Constructor injection instead of field injection
        public AgentController(@Lazy A2AAgentRegistry agentRegistry) {
                this.agentRegistry = agentRegistry;
        }

        // Simple in-memory task storage for demo
        private final Map<String, Task> tasks = new ConcurrentHashMap<>();

        // @GetMapping(value = "/agent/card", produces =
        // MediaType.APPLICATION_JSON_VALUE)
        // public Mono<AgentCard> getAgentCard() {
        // List<A2AAgentRegistry.AgentMeta> agents = new
        // ArrayList<>(agentRegistry.getAgentRegistry().values());
        // if (agents.isEmpty()) {
        // // Return a default card if no agents are registered yet
        // return Mono.just(AgentCard.builder()
        // .name("A2A Agent")
        // .version("1.0")
        // .description("A2A Protocol Agent")
        // .skills(Collections.emptyList())
        // .defaultInputModes(Collections.singletonList("text/plain"))
        // .defaultOutputModes(Collections.singletonList("text/plain"))
        // .capabilities(AgentCapabilities.builder()
        // .streaming(true)
        // .pushNotifications(false)
        // .stateTransitionHistory(true)
        // .build())
        // .provider(AgentProvider.builder()
        // .organization("A2A System")
        // .url("http://localhost:8080")
        // .build())
        // .url("http://localhost:8080")
        // .build());
        // }

        // A2AAgentRegistry.AgentMeta meta = agents.get(0);
        // List<AgentSkill> allSkills = agents.stream()
        // .flatMap(agent -> agent.getSkills().stream().map(skillMeta ->
        // AgentSkill.builder()
        // .id(skillMeta.getId())
        // .name(skillMeta.getName())
        // .description(skillMeta.getDescription())
        // .tags(Arrays.asList(skillMeta.getTags()))
        // .build()))
        // .collect(Collectors.toList());

        // AgentCard card = AgentCard.builder()
        // .name(meta.getName())
        // .version(meta.getVersion())
        // .description(meta.getDescription())
        // .skills(allSkills)
        // .defaultInputModes(Collections.singletonList("text/plain"))
        // .defaultOutputModes(Collections.singletonList("text/plain"))
        // .capabilities(AgentCapabilities.builder()
        // .streaming(true)
        // .pushNotifications(false)
        // .stateTransitionHistory(true)
        // .build())
        // .provider(AgentProvider.builder()
        // .organization("A2A Auto MultiAgent System")
        // .url("http://localhost:8080")
        // .build())
        // .url("http://localhost:8080")
        // .build();
        // return Mono.just(card);
        // }

        @PostMapping(value = "/agent/message", consumes = MediaType.APPLICATION_JSON_VALUE)
        public Mono<SendMessageSuccessResponse> handleSendMessage(@RequestBody SendMessageRequest request) {
                log.info("(KK777) Received message request: {}", request);
                return processMessage(request.getParams())
                                .map(task -> SendMessageSuccessResponse.builder()
                                                .id(request.getId())
                                                .result(createResponseMessage(task))
                                                .build())
                                .onErrorReturn(SendMessageSuccessResponse.builder()
                                                .id(request.getId())
                                                .result(Message.builder()
                                                                .kind("message")
                                                                .messageId(UUID.randomUUID().toString())
                                                                .role("agent")
                                                                .parts(Collections.singletonList(TextPart.builder()
                                                                                .text("Error processing request")
                                                                                .build()))
                                                                .build())
                                                .build());
        }

        @PostMapping(value = "/agent/stream", consumes = MediaType.APPLICATION_JSON_VALUE)
        public Flux<Object> handleStreamingMessage(@RequestBody SendStreamingMessageRequest request) {
                return processMessage(request.getParams())
                                .flatMapMany(task -> Flux.just(
                                                createTaskStatusUpdate(task),
                                                createResponseMessage(task)))
                                .onErrorReturn(Map.of("error", "Failed to process streaming request"));
        }

        @PostMapping(value = "/agent/tasks/get", consumes = MediaType.APPLICATION_JSON_VALUE)
        public Mono<GetTaskResponse> handleGetTask(@RequestBody GetTaskRequest request) {
                String taskId = request.getParams().getId();
                Task task = tasks.get(taskId);

                if (task == null) {
                        return Mono.just(GetTaskResponse.builder()
                                        .id(request.getId())
                                        .error(A2AError.builder()
                                                        .code(-32001)
                                                        .message("Task not found")
                                                        .build())
                                        .build());
                }

                return Mono.just(GetTaskResponse.builder()
                                .id(request.getId())
                                .result(task)
                                .build());
        }

        @PostMapping(value = "/agent/tasks/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
        public Mono<CancelTaskResponse> handleCancelTask(@RequestBody CancelTaskRequest request) {
                String taskId = request.getParams().getId();
                Task task = tasks.get(taskId);

                if (task == null) {
                        return Mono.just(CancelTaskResponse.builder()
                                        .id(request.getId())
                                        .error(A2AError.builder()
                                                        .code(-32001)
                                                        .message("Task not found")
                                                        .build())
                                        .build());
                }

                // Update task status to canceled
                task.setStatus(TaskStatus.builder()
                                .state(TaskState.CANCELED)
                                .timestamp(LocalDateTime.now().toString())
                                .build());

                return Mono.just(CancelTaskResponse.builder()
                                .id(request.getId())
                                .result(task)
                                .build());
        }

        private Mono<Task> processMessage(MessageSendParams params) {
                log.info("(KK777) Processing message: " + params);
                if (params == null || params.getMessage() == null) {
                        return Mono.error(new IllegalArgumentException("Invalid message parameters"));
                }

                Message msg = params.getMessage();
                String skillId = msg.getTaskId();

                // Create or find task
                String taskId = msg.getTaskId() != null ? msg.getTaskId() : UUID.randomUUID().toString();
                Task task = tasks.computeIfAbsent(taskId, id -> Task.builder()
                                .id(id)
                                .contextId(msg.getContextId() != null ? msg.getContextId()
                                                : UUID.randomUUID().toString())
                                .status(TaskStatus.builder()
                                                .state(TaskState.WORKING)
                                                .timestamp(LocalDateTime.now().toString())
                                                .build())
                                .history(new ArrayList<>())
                                .build());

                // Extract input from message parts
                String input = extractInputFromMessage(msg);

                // Find and execute skill
                Map<String, A2AAgentRegistry.AgentMeta> agents = agentRegistry.getAgentRegistry();
                log.info("(KK777) expecting skill for agent: " + agents.entrySet().toString() + " for skill: "
                                + skillId);
                for (A2AAgentRegistry.AgentMeta agent : agents.values()) {
                        Optional<A2AAgentRegistry.SkillMeta> skillOpt = agent.getSkills().stream()
                                        .filter(meta -> meta.getId().equals(skillId))
                                        .findFirst();

                        if (skillOpt.isPresent()) {
                                log.info("(KK777) Found skill: " + skillId + " in agent: " + agent.getName());
                                try {
                                        A2AAgentRegistry.SkillMeta skill = skillOpt.get();
                                        Object result;

                                        // Handle different parameter types
                                        Class<?>[] paramTypes = skill.getMethod().getParameterTypes();
                                        if (paramTypes.length == 0) {
                                                result = skill.getMethod().invoke(agent.getBean());
                                                log.info("(KK888-EX-1) Skill " + skillId + " executed with result: "
                                                                + result);
                                        } else if (paramTypes.length == 1) {
                                                result = skill.getMethod().invoke(agent.getBean(), input);
                                                if (result instanceof CompletableFuture) {
                                                        result = ((CompletableFuture<?>) result).get(); // Blocking wait
                                                }
                                                log.info("(KK888-EX-2) Skill " + skillId + " executed with result: "
                                                                + result);
                                        } else {
                                                // For multiple parameters, you might need more sophisticated parameter
                                                // mapping
                                                result = skill.getMethod().invoke(agent.getBean(), input);
                                                log.info(input + " (KK888-EX-3) MP Skill " + skillId
                                                                + " executed with result: " + result);
                                        }

                                        // Store result in task metadata
                                        if (task.getMetadata() == null) {
                                                task.setMetadata(new HashMap<>());
                                        }
                                        log.info("(KK888) Skill " + skillId + " executed with result: "
                                                        + result.toString());
                                        task.getMetadata().put("result", result != null ? result.toString() : "null");

                                        // Update task status
                                        task.setStatus(TaskStatus.builder()
                                                        .state(TaskState.COMPLETED)
                                                        .timestamp(LocalDateTime.now().toString())
                                                        .build());
                                        log.info("(KK999) Skill " + skillId + " executed successfully with result: "
                                                        + task.getMetadata().get("result"));
                                        return Mono.just(task);
                                } catch (Exception e) {
                                        System.err.println("Error executing skill " + skillId + ": " + e.getMessage());
                                        task.setStatus(TaskStatus.builder()
                                                        .state(TaskState.FAILED)
                                                        .timestamp(LocalDateTime.now().toString())
                                                        .build());
                                        if (task.getMetadata() == null) {
                                                task.setMetadata(new HashMap<>());
                                        }
                                        task.getMetadata().put("error", e.getMessage());
                                        return Mono.just(task);
                                }
                        }
                }

                task.setStatus(TaskStatus.builder()
                                .state(TaskState.REJECTED)
                                .timestamp(LocalDateTime.now().toString())
                                .build());
                if (task.getMetadata() == null) {
                        task.setMetadata(new HashMap<>());
                }
                task.getMetadata().put("error", "Skill not found: " + skillId);
                return Mono.just(task);
        }

        private String extractInputFromMessage(Message msg) {
                if (msg.getParts() != null && !msg.getParts().isEmpty()) {
                        Part firstPart = msg.getParts().get(0);
                        if (firstPart instanceof TextPart) {
                                return ((TextPart) firstPart).getText();
                        }
                }
                return "";
        }

        private Message createResponseMessage(Task task) {
                String resultText = "Task completed";
                if (task.getMetadata() != null) {
                        Object result = task.getMetadata().get("result");
                        if (result != null) {
                                resultText = result.toString();
                        }
                        Object error = task.getMetadata().get("error");
                        if (error != null) {
                                resultText = "Error: " + error.toString();
                        }
                }

                return Message.builder()
                                .kind("message")
                                .messageId(UUID.randomUUID().toString())
                                .role("agent")
                                .contextId(task.getContextId())
                                .taskId(task.getId())
                                .parts(Collections.singletonList(TextPart.builder()
                                                .text(resultText)
                                                .build()))
                                .build();
        }

        private Object createTaskStatusUpdate(Task task) {
                return Map.of(
                                "kind", "status-update",
                                "taskId", task.getId(),
                                "contextId", task.getContextId(),
                                "status", task.getStatus(),
                                "final", task.getStatus().getState() == TaskState.COMPLETED ||
                                                task.getStatus().getState() == TaskState.FAILED ||
                                                task.getStatus().getState() == TaskState.CANCELED);
        }

        @GetMapping(value = "/agent/card", produces = MediaType.APPLICATION_JSON_VALUE)
        public Mono<AgentCard> getAgentCard() {
                List<A2AAgentRegistry.AgentMeta> agents = new ArrayList<>(agentRegistry.getAgentRegistry().values());

                if (agents.isEmpty()) {
                        return Mono.just(createDefaultAgentCard());
                }

                A2AAgentRegistry.AgentMeta primaryAgent = agents.get(0);
                List<AgentSkill> allSkills = agents.stream()
                                .flatMap(agent -> agent.getSkills().stream().map(this::convertToAgentSkill))
                                .collect(Collectors.toList());

                AgentCard card = AgentCard.builder()
                                .name(primaryAgent.getName())
                                .version(primaryAgent.getVersion())
                                .description(primaryAgent.getDescription())
                                .url(primaryAgent.getUrl())
                                .skills(allSkills)
                                .defaultInputModes(Collections.singletonList("text/plain"))
                                .defaultOutputModes(Collections.singletonList("text/plain"))
                                .capabilities(createDefaultCapabilities())
                                .provider(createDefaultProvider())
                                .build();

                return Mono.just(card);
        }

        private AgentSkill convertToAgentSkill(A2AAgentRegistry.SkillMeta skillMeta) {
                return AgentSkill.builder()
                                .id(skillMeta.getId())
                                .name(skillMeta.getName())
                                .description(skillMeta.getDescription())
                                .tags(Arrays.asList(skillMeta.getTags()))
                                .build();
        }

        private AgentCard createDefaultAgentCard() {
                return AgentCard.builder()
                                .name("A2A Agent")
                                .version("1.0")
                                .description("A2A Protocol Agent")
                                .url("http://localhost:8080")
                                .skills(Collections.emptyList())
                                .defaultInputModes(Collections.singletonList("text/plain"))
                                .defaultOutputModes(Collections.singletonList("text/plain"))
                                .capabilities(createDefaultCapabilities())
                                .provider(createDefaultProvider())
                                .build();
        }

        private AgentCapabilities createDefaultCapabilities() {
                return AgentCapabilities.builder()
                                .streaming(true)
                                .pushNotifications(false)
                                .stateTransitionHistory(true)
                                .build();
        }

        private AgentProvider createDefaultProvider() {
                return AgentProvider.builder()
                                .organization("A2A System")
                                .url("http://localhost:8080")
                                .build();
        }
}