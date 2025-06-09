// src/main/java/net/kaduk/a2a/AgentController.java
package net.kaduk.a2a;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class AgentController {

    @Autowired
    private A2AAgentRegistry agentRegistry;

    // Simple in-memory task storage for demo
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    @GetMapping(value = "/agent/card", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AgentCard> getAgentCard() {
        List<A2AAgentRegistry.AgentMeta> agents = new ArrayList<>(agentRegistry.getAgentRegistry().values());
        if (agents.isEmpty()) {
            return Mono.empty();
        }

        A2AAgentRegistry.AgentMeta meta = agents.get(0);
        List<AgentSkill> allSkills = agents.stream()
                .flatMap(agent -> agent.getSkills().stream().map(skillMeta ->
                        AgentSkill.builder()
                                .id(skillMeta.getId())
                                .name(skillMeta.getName())
                                .description(skillMeta.getDescription())
                                .tags(Arrays.asList(skillMeta.getTags()))
                                .build()
                )).collect(Collectors.toList());

        AgentCard card = AgentCard.builder()
                .name(meta.getName())
                .version(meta.getVersion())
                .description(meta.getDescription())
                .skills(allSkills)
                .defaultInputModes(Collections.singletonList("text/plain"))
                .defaultOutputModes(Collections.singletonList("text/plain"))
                .capabilities(AgentCapabilities.builder()
                        .streaming(true)
                        .pushNotifications(false)
                        .stateTransitionHistory(true)
                        .build())
                .provider(AgentProvider.builder()
                        .organization("A2A Auto MultiAgent System")
                        .url("http://localhost:8080")
                        .build())
                .url("http://localhost:8080")
                .build();
        return Mono.just(card);
    }

    @PostMapping(value = "/agent/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SendMessageSuccessResponse> handleSendMessage(@RequestBody SendMessageRequest request) {
        return processMessage(request.getParams())
                .map(task -> SendMessageSuccessResponse.builder()
                        .id(request.getId())
                        .result(createResponseMessage(task))
                        .build());
    }

    @PostMapping(value = "/agent/stream", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Object> handleStreamingMessage(@RequestBody SendStreamingMessageRequest request) {
        return processMessage(request.getParams())
                .flatMapMany(task -> Flux.just(
                        createTaskStatusUpdate(task),
                        createResponseMessage(task)
                ));
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
        Message msg = params.getMessage();
        String skillId = msg.getTaskId();

        // Create or find task
        String taskId = msg.getTaskId() != null ? msg.getTaskId() : UUID.randomUUID().toString();
        Task task = tasks.computeIfAbsent(taskId, id -> Task.builder()
                .id(id)
                .contextId(msg.getContextId() != null ? msg.getContextId() : UUID.randomUUID().toString())
                .status(TaskStatus.builder()
                        .state(TaskState.WORKING)
                        .timestamp(LocalDateTime.now().toString())
                        .build())
                .history(new ArrayList<>())
                .build());

        // Find and execute skill
        for (A2AAgentRegistry.AgentMeta agent : agentRegistry.getAgentRegistry().values()) {
            Optional<A2AAgentRegistry.SkillMeta> skillOpt = agent.getSkills().stream()
                    .filter(meta -> meta.getId().equals(skillId))
                    .findFirst();
            
            if (skillOpt.isPresent()) {
                try {
                    // Execute skill (simplified - you may need to extract parameters from message parts)
                    Object result = skillOpt.get().getMethod().invoke(agent.getBean(), "test input");
                    
                    // Update task status
                    task.setStatus(TaskStatus.builder()
                            .state(TaskState.COMPLETED)
                            .timestamp(LocalDateTime.now().toString())
                            .build());
                    
                    return Mono.just(task);
                } catch (Exception e) {
                    task.setStatus(TaskStatus.builder()
                            .state(TaskState.FAILED)
                            .timestamp(LocalDateTime.now().toString())
                            .build());
                    return Mono.just(task);
                }
            }
        }

        task.setStatus(TaskStatus.builder()
                .state(TaskState.REJECTED)
                .timestamp(LocalDateTime.now().toString())
                .build());
        return Mono.just(task);
    }

    private Message createResponseMessage(Task task) {
        return Message.builder()
                .kind("message")
                .messageId(UUID.randomUUID().toString())
                .role("agent")
                .contextId(task.getContextId())
                .taskId(task.getId())
                .parts(Collections.singletonList(TextPart.builder()
                        .text("Task processed with status: " + task.getStatus().getState().getValue())
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
                        task.getStatus().getState() == TaskState.CANCELED
        );
    }
}