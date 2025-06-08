package net.kaduk.a2a;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

////////////////////////////////////////////////////////////////
// Worker Agent 1
////////////////////////////////////////////////////////////////
@A2AAgent(name = "WorkerA", version = "1.0", description = "Performs data transformation.")
@Component
class WorkerA {

    @A2AAgentSkill(id = "transform", name = "Transform", description = "Transforms input to uppercase.", tags = {"worker"})
    public String transform(String input) {
        return input.toUpperCase();
    }
}

////////////////////////////////////////////////////////////////
// Worker Agent 2
////////////////////////////////////////////////////////////////
@A2AAgent(name = "WorkerB", version = "1.0", description = "Performs data aggregation.")
@Component
class WorkerB {

    @A2AAgentSkill(id = "aggregate", name = "Aggregate", description = "Counts characters.", tags = {"worker"})
    public int aggregate(String input) {
        return input.length();
    }
}

////////////////////////////////////////////////////////////////
// Coordinator Agent (invokes workers according to plan)
////////////////////////////////////////////////////////////////
@A2AAgent(name = "Coordinator", version = "1.0", description = "Coordinates worker agents via DAG plan.")
@Component
class Coordinator {

    @Autowired
    private A2AWebClientService a2aClient; // auto-configured WebClient-based service

    // Example: Coordinator's skill that orchestrates calls to WorkerA and WorkerB according to a DAG plan
    @A2AAgentSkill(id = "coordinate", name = "Coordinate DAG Processing", description = "Executes DAG plan with two workers", tags = {"coordinator"})
    public Mono<String> executeWorkflow(String input, String workerAUrl, String workerBUrl) {
        // Step 1: Call WorkerA (transform task)
        SendMessageRequest requestA = SendMessageRequest.builder()
                .id("reqA-" + UUID.randomUUID())
                .params(MessageSendParams.builder()
                    .message(Message.builder()
                        .kind("message")
                        .messageId("msg-" + UUID.randomUUID())
                        .role("coordinator")
                        .parts(Collections.emptyList()) // can use part for input if needed
                        .taskId("transform")
                        .build())
                    .build())
                .build();

        // step 2 depends on result of workerA
        return a2aClient.sendMessage(workerAUrl, requestA)
            .flatMap(respA -> {
                // Normally you would parse respA for content; here just simulate next call
                String transformed = input.toUpperCase(); // simulate WorkerA transform
                SendMessageRequest requestB = SendMessageRequest.builder()
                        .id("reqB-" + UUID.randomUUID())
                        .params(MessageSendParams.builder()
                                .message(Message.builder()
                                        .kind("message")
                                        .messageId("msg-" + UUID.randomUUID())
                                        .role("coordinator")
                                        .parts(Collections.emptyList())
                                        .taskId("aggregate")
                                        .build())
                                .build())
                        .build();
                return a2aClient.sendMessage(workerBUrl, requestB)
                    .map(respB -> {
                        // Simulate aggregate (normally parse respB to get actual result)
                        int count = transformed.length(); // simulate WorkerB aggregate
                        // Consolidate final result
                        return "Final result: [" + transformed + "] has length " + count;
                    });
            });
    }
}
