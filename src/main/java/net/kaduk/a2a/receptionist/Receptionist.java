package net.kaduk.a2a.receptionist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.kaduk.a2a.A2AWebClientService;
import net.kaduk.a2a.AgentCapabilityInfo;
import net.kaduk.a2a.Message;
import net.kaduk.a2a.MessageSendConfiguration;
import net.kaduk.a2a.MessageSendParams;
import net.kaduk.a2a.SendMessageRequest;
import net.kaduk.a2a.SkillCapability;
import net.kaduk.a2a.SkillInvocationRequest;
import net.kaduk.a2a.SkillInvocationResponse;
import net.kaduk.a2a.TextPart;
import net.kaduk.a2a.receptionist.model.AgentEntity;
import net.kaduk.a2a.receptionist.model.AgentSkillDTO;
import net.kaduk.a2a.receptionist.model.AgentSkillDocument;
import net.kaduk.a2a.receptionist.model.CapabilityQuery;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class Receptionist {

    private final AgentRepositoryCustom agentRepository;
    private final A2AWebClientService webClientService;
    private final ObjectMapper objectMapper;

    public Receptionist(AgentRepositoryCustom agentRepository,
            A2AWebClientService webClientService,
            ObjectMapper objectMapper) {
        this.agentRepository = agentRepository;
        this.webClientService = webClientService;
        this.objectMapper = objectMapper;
    }

    public Mono<List<AgentCapabilityInfo>> findAgentsByCapability(CapabilityQuery capabilityQuery) {
        return Mono.fromSupplier(() -> {
            List<AgentCapabilityInfo> matchingAgents = new ArrayList<>();
            List<AgentEntity> entities = agentRepository.searchByCapability(capabilityQuery);

            for (AgentEntity entity : entities) {
                try {
                    AgentSkillDocument doc = objectMapper.readValue(entity.getSkill(), AgentSkillDocument.class);
                    List<SkillCapability> matchingSkills = doc.getSkills().stream()
                            .filter(skill -> isSkillMatching(skill, capabilityQuery))
                            .map(this::convertToSkillCapability)
                            .collect(Collectors.toList());

                    if (!matchingSkills.isEmpty()) {
                        matchingAgents.add(AgentCapabilityInfo.builder()
                                .agentName(entity.getName())
                                .agentVersion(entity.getVersion())
                                .agentUrl(entity.getUrl())
                                .description(entity.getDescription())
                                .availableSkills(matchingSkills)
                                .confidence(calculateConfidence(matchingSkills, capabilityQuery))
                                .build());
                    }
                } catch (Exception e) {
                    log.warn("Skill JSON parsing failed for {}: {}", entity.getName(), e.getMessage());
                }
            }

            matchingAgents.sort(Comparator.comparingDouble(AgentCapabilityInfo::getConfidence).reversed());
            return matchingAgents;
        });
    }

    public Mono<Optional<AgentCapabilityInfo>> findBestAgentForCapability(CapabilityQuery capabilityQuery) {
        return findAgentsByCapability(capabilityQuery)
                .map(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)));
    }

    public Mono<SkillInvocationResponse> invokeAgentSkill(SkillInvocationRequest request) {
        return Mono.fromSupplier(() -> {
            log.info(String.format("(KK) Invoking skill '%s' on agent '%s'", request.getSkillId(), request.getAgentName()));
            Optional<AgentEntity> agentOpt = agentRepository.findByName(request.getAgentName());

            if (agentOpt.isEmpty()) {
                return Mono.just(SkillInvocationResponse.builder()
                        .success(false)
                        .errorMessage("Agent not found: " + request.getAgentName())
                        .build());
            }

            AgentEntity agent = agentOpt.get();
            SendMessageRequest messageRequest = createMessageRequest(request, agent.getUrl());
            log.info(String.format("(KK) Sending message to agent %s at %s", agent.getName(), agent.getUrl()));
            return webClientService.sendMessage(agent.getUrl(), messageRequest)
                    .map(response -> SkillInvocationResponse.builder()
                            .success(true)
                            .result(response.getResult())
                            .taskId(response.getResult().getTaskId())
                            .build())
                    .onErrorReturn(SkillInvocationResponse.builder()
                            .success(false)
                            .errorMessage("Skill invocation failed")
                            .build());
        }).flatMap(m -> m);
    }

    public Mono<List<AgentCapabilityInfo>> discoverAllCapabilities() {
        return Mono.fromSupplier(() -> {
            CapabilityQuery emptyQuery = new CapabilityQuery();
            List<AgentEntity> entities = agentRepository.searchByCapability(emptyQuery);

            return entities.stream().map(entity -> {
                try {
                    AgentSkillDocument doc = objectMapper.readValue(entity.getSkill(), AgentSkillDocument.class);
                    List<SkillCapability> allSkills = doc.getSkills().stream()
                            .map(this::convertToSkillCapability)
                            .collect(Collectors.toList());

                    return AgentCapabilityInfo.builder()
                            .agentName(entity.getName())
                            .agentVersion(entity.getVersion())
                            .agentUrl(entity.getUrl())
                            .description(entity.getDescription())
                            .availableSkills(allSkills)
                            .confidence(1.0)
                            .build();
                } catch (Exception e) {
                    log.error("Failed to parse skills for {}: {}", entity.getName(), e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        });
    }

    // --- Private Utility Methods ---

    private boolean isSkillMatching(AgentSkillDTO skill, CapabilityQuery query) {
        if (query.getSkillId() != null && skill.getId().equals(query.getSkillId()))
            return true;

        if (query.getRequiredTags() != null && !query.getRequiredTags().isEmpty()) {
            Set<String> tags = skill.getTags() != null ? new HashSet<>(skill.getTags()) : Set.of();
            if (query.getRequiredTags().stream().anyMatch(tags::contains))
                return true;
        }

        if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
            String search = (skill.getName() + " " + skill.getDescription()).toLowerCase();
            return query.getKeywords().stream().anyMatch(k -> search.contains(k.toLowerCase()));
        }

        return false;
    }

    private SkillCapability convertToSkillCapability(AgentSkillDTO skill) {
        return SkillCapability.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .description(skill.getDescription())
                .tags(skill.getTags())
                .inputModes(List.of("text/plain"))
                .outputModes(List.of("text/plain"))
                .build();
    }

    private double calculateConfidence(List<SkillCapability> skills, CapabilityQuery query) {
        if (skills.isEmpty())
            return 0.0;
        double score = 0.5 + skills.size() * 0.1;

        if (query.getSkillId() != null &&
                skills.stream().anyMatch(s -> s.getSkillId().equals(query.getSkillId())))
            score += 0.3;

        return Math.min(score, 1.0);
    }

    private SendMessageRequest createMessageRequest(SkillInvocationRequest request, String agentUrl) {
        Message msg = Message.builder()
                .kind("message")
                .messageId(UUID.randomUUID().toString())
                .role("user")
                .taskId(request.getSkillId())
                .contextId(UUID.randomUUID().toString())
                .parts(List.of(TextPart.builder().text(
                        Optional.ofNullable(request.getInput()).orElse("")).build()))
                .build();

        return SendMessageRequest.builder()
                .id(UUID.randomUUID().toString())
                .params(MessageSendParams.builder()
                        .message(msg)
                        .configuration(MessageSendConfiguration.builder()
                                .acceptedOutputModes(List.of("text/plain"))
                                .blocking(true)
                                .build())
                        .build())
                .build();
    }
}
