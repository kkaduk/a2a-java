// src/main/java/net/kaduk/a2a/Receptionist.java
package net.kaduk.a2a.receptionist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import net.kaduk.a2a.*;
import net.kaduk.a2a.receptionist.model.A2AAgentSkillDTO;
import net.kaduk.a2a.receptionist.model.AgentEntity;
import net.kaduk.a2a.receptionist.model.CapabilityQuery;

/**
 * Receptionist handles capability-based queries from external agent platforms.
 * Enables discovery and invocation of registered agents based on their skills
 * and capabilities.
 */
@Component
@Slf4j
public class Receptionist {

    private final AgentRepository agentRepository;
    private final A2AAgentRegistry agentRegistry;
    private final A2AWebClientService webClientService;

    public Receptionist(AgentRepository agentRepository,
            A2AAgentRegistry agentRegistry,
            A2AWebClientService webClientService) {
        this.agentRepository = agentRepository;
        this.agentRegistry = agentRegistry;
        this.webClientService = webClientService;
    }

    /**
     * Finds agents that possess the requested capability/skill.
     * 
     * @param capabilityQuery Query containing capability requirements
     * @return List of matching agent capabilities
     */
    public Mono<List<AgentCapabilityInfo>> findAgentsByCapability(CapabilityQuery capabilityQuery) {
        log.debug("(KK2) Processing capability query: {}", capabilityQuery);

        return Mono.fromSupplier(() -> {
            log.info("(KK) MONO SUPPLIER EXECUTING");
            List<AgentCapabilityInfo> matchingAgents = new ArrayList<>();

            // Search through registered agents
            // Fixme: Use agent database instead of in-memory registry + AgentCard
            Map<String, A2AAgentRegistry.AgentMeta> agents = agentRegistry.getAgentRegistry();

    ;
            List<AgentEntity> agentsRepo =  agentRepository.findAll();
            agentsRepo.forEach(a -> {
                var xxx = a.getUrl();
            });
            log.debug("(KK) Finding agents: {}", agents.keySet());

            for (A2AAgentRegistry.AgentMeta agent : agents.values()) {
                List<SkillCapability> matchingSkills = findMatchingSkills(agent, capabilityQuery);

                if (!matchingSkills.isEmpty()) {
                    AgentCapabilityInfo capabilityInfo = AgentCapabilityInfo.builder()
                            .agentName(agent.getName())
                            .agentVersion(agent.getVersion())
                            .agentUrl(agent.getUrl())
                            .description(agent.getDescription())
                            .availableSkills(matchingSkills)
                            .confidence(calculateConfidence(matchingSkills, capabilityQuery))
                            .build();

                    matchingAgents.add(capabilityInfo);
                }
            }

            // Sort by confidence score (highest first)
            matchingAgents.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

            log.info("Found {} agents matching capability query", matchingAgents.size());
            return matchingAgents;
        });
    }

    /**
     * Finds the best agent for a specific capability.
     * 
     * @param capabilityQuery Query containing capability requirements
     * @return Optional containing the best matching agent
     */
    public Mono<Optional<AgentCapabilityInfo>> findBestAgentForCapability(CapabilityQuery capabilityQuery) {
        return findAgentsByCapability(capabilityQuery)
                .map(agents -> agents.isEmpty() ? Optional.empty() : Optional.of(agents.get(0)));
    }

    /**
     * Invokes a specific skill on a discovered agent.
     * 
     * @param invocationRequest Request containing agent and skill information
     * @return Response from the invoked agent
     */
    public Mono<SkillInvocationResponse> invokeAgentSkill(SkillInvocationRequest invocationRequest) {
        log.debug("Invoking skill {} on agent {}",
                invocationRequest.getSkillId(),
                invocationRequest.getAgentName());

        // Find the target agent
        Optional<A2AAgentRegistry.AgentMeta> targetAgent = agentRegistry.getAgentRegistry().values()
                .stream()
                .filter(agent -> agent.getName().equals(invocationRequest.getAgentName()))
                .findFirst();

        if (targetAgent.isEmpty()) {
            return Mono.just(SkillInvocationResponse.builder()
                    .success(false)
                    .errorMessage("Agent not found: " + invocationRequest.getAgentName())
                    .build());
        }

        // Create A2A message request
        SendMessageRequest messageRequest = createMessageRequest(invocationRequest, targetAgent.get());

        // Invoke via A2A protocol
        return webClientService.sendMessage(targetAgent.get().getUrl(), messageRequest)
                .map(response -> SkillInvocationResponse.builder()
                        .success(true)
                        .result(response.getResult())
                        .taskId(response.getResult().getTaskId())
                        .build())
                .onErrorReturn(SkillInvocationResponse.builder()
                        .success(false)
                        .errorMessage("Failed to invoke skill")
                        .build());
    }

    /**
     * Discovers all available capabilities in the system.
     * 
     * @return List of all available agent capabilities
     */
    public Mono<List<AgentCapabilityInfo>> discoverAllCapabilities() {
        return Mono.fromSupplier(() -> {
            List<AgentCapabilityInfo> allCapabilities = new ArrayList<>();

            Map<String, A2AAgentRegistry.AgentMeta> agents = agentRegistry.getAgentRegistry();

            for (A2AAgentRegistry.AgentMeta agent : agents.values()) {
                List<SkillCapability> skills = agent.getSkills().stream()
                        .map(this::convertToSkillCapability)
                        .collect(Collectors.toList());

                AgentCapabilityInfo capabilityInfo = AgentCapabilityInfo.builder()
                        .agentName(agent.getName())
                        .agentVersion(agent.getVersion())
                        .agentUrl(agent.getUrl())
                        .description(agent.getDescription())
                        .availableSkills(skills)
                        .confidence(1.0) // Full confidence for discovery
                        .build();

                allCapabilities.add(capabilityInfo);
            }

            return allCapabilities;
        });
    }

    // Private helper methods

    private List<SkillCapability> findMatchingSkills(A2AAgentRegistry.AgentMeta agent,
            CapabilityQuery query) {
        List<SkillCapability> matchingSkills = new ArrayList<>();

        System.out.println("KK ******** Finding matching skills for agent: " + agent.getName() + ", query: " + query);

        for (A2AAgentRegistry.SkillMeta skill : agent.getSkills()) {
            if (isSkillMatching(skill, query)) {
                SkillCapability capability = convertToSkillCapability(skill);
                matchingSkills.add(capability);
            }
        }

        return matchingSkills;
    }

    private boolean isSkillMatching(A2AAgentRegistry.SkillMeta skill, CapabilityQuery query) {
        System.out.println("Checking skill: " + skill.getName() + ", " + skill.getId() + " against query: "
                + query.getSkillId() + ", " + query.getRequiredTags() + ", " + query.getKeywords());
        // Check skill ID match
        if (query.getSkillId() != null && skill.getId().equals(query.getSkillId())) {
            return true;
        }

        // Check tag matches
        if (query.getRequiredTags() != null && !query.getRequiredTags().isEmpty()) {
            Set<String> skillTags = Set.of(skill.getTags());
            return query.getRequiredTags().stream().anyMatch(skillTags::contains);
        }

        // Check keyword matches in name or description
        if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
            String searchText = (skill.getName() + " " + skill.getDescription()).toLowerCase();
            return query.getKeywords().stream()
                    .anyMatch(keyword -> searchText.contains(keyword.toLowerCase()));
        }

        return false;
    }

    private boolean isSkillMatching(A2AAgentSkillDTO skill, CapabilityQuery query) {
    
    // Check skill ID match
    if (query.getSkillId() != null && skill.getId().equals(query.getSkillId())) {
        return true;
    }

    // Check tag matches
    if (query.getRequiredTags() != null && !query.getRequiredTags().isEmpty()) {
        List<String> skillTagsList = skill.getTags();
        if (skillTagsList != null) {
            Set<String> skillTags = new HashSet<>(skillTagsList);
            if (query.getRequiredTags().stream().anyMatch(skillTags::contains)) {
                return true;
            }
        }
    }

    // Check keyword matches in name or description
    if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
        String name = skill.getName() != null ? skill.getName() : "";
        String description = skill.getDescription() != null ? skill.getDescription() : "";
        String searchText = (name + " " + description).toLowerCase();

        return query.getKeywords().stream()
                .anyMatch(keyword -> searchText.contains(keyword.toLowerCase()));
    }

    return false;
}


    private SkillCapability convertToSkillCapability(A2AAgentRegistry.SkillMeta skill) {
        return SkillCapability.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .description(skill.getDescription())
                .tags(Arrays.asList(skill.getTags()))
                .inputModes(Collections.singletonList("text/plain")) // Default
                .outputModes(Collections.singletonList("text/plain")) // Default
                .build();
    }

    private double calculateConfidence(List<SkillCapability> matchingSkills,
            CapabilityQuery query) {
        if (matchingSkills.isEmpty()) {
            return 0.0;
        }

        // Simple confidence calculation based on number of matches
        double baseConfidence = 0.5;
        double skillMatchBonus = 0.1 * matchingSkills.size();

        // Bonus for exact skill ID match
        if (query.getSkillId() != null) {
            boolean exactMatch = matchingSkills.stream()
                    .anyMatch(skill -> skill.getSkillId().equals(query.getSkillId()));
            if (exactMatch) {
                baseConfidence += 0.3;
            }
        }

        return Math.min(1.0, baseConfidence + skillMatchBonus);
    }

    private SendMessageRequest createMessageRequest(SkillInvocationRequest invocationRequest,
            A2AAgentRegistry.AgentMeta agent) {
        Message message = Message.builder()
                .kind("message")
                .messageId(UUID.randomUUID().toString())
                .role("user")
                .taskId(invocationRequest.getSkillId())
                .contextId(UUID.randomUUID().toString())
                .parts(Collections.singletonList(TextPart.builder()
                        .text(invocationRequest.getInput() != null ? invocationRequest.getInput() : "")
                        .build()))
                .build();

        MessageSendParams params = MessageSendParams.builder()
                .message(message)
                .configuration(MessageSendConfiguration.builder()
                        .acceptedOutputModes(Collections.singletonList("text/plain"))
                        .blocking(true)
                        .build())
                .build();

        return SendMessageRequest.builder()
                .id(UUID.randomUUID().toString())
                .params(params)
                .build();
    }
}