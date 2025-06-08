package net.kaduk.a2a;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AgentController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping(value = "/agent/card", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AgentCard> getAgentCard() {
        List<A2AAgentRegistry.AgentMeta> agents = new ArrayList<>(A2AAgentRegistry.getRegistry(applicationContext).values());
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
                .provider(AgentProvider.builder()
                        .organization("A2A Auto MultiAgent System")
                        .url("http://localhost:8080")
                        .build())
                .build();
        return Mono.just(card);
    }

    @PostMapping(
        value = "/agent/message",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<SendMessageSuccessResponse> handleAgentMessage(@RequestBody SendMessageRequest request) {
        List<A2AAgentRegistry.AgentMeta> agents = new ArrayList<>(A2AAgentRegistry.getRegistry(applicationContext).values());
        if (request.getParams() == null || request.getParams().getMessage() == null) {
            return Mono.error(new IllegalArgumentException("Missing message data"));
        }
        Message msg = request.getParams().getMessage();
        String skillId = msg.getTaskId();

        for (A2AAgentRegistry.AgentMeta agent : agents) {
            Optional<A2AAgentRegistry.SkillMeta> skillOpt = agent.getSkills().stream()
                    .filter(meta -> meta.getId().equals(skillId))
                    .findFirst();
            if (skillOpt.isPresent()) {
                try {
                    Object result = skillOpt.get().getMethod().invoke(agent.getBean(), "test"); // Dummy arg for now
                    Message responseMsg = Message.builder()
                            .kind("message")
                            .messageId(UUID.randomUUID().toString())
                            .role("agent")
                            .parts(msg.getParts())
                            .contextId(msg.getContextId())
                            .taskId(skillId)
                            .build();

                    SendMessageSuccessResponse response = SendMessageSuccessResponse.builder()
                            .id(request.getId())
                            .result(responseMsg)
                            .build();
                    return Mono.just(response);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            }
        }
        return Mono.error(new IllegalArgumentException("Skill not found: " + skillId));
    }
}
