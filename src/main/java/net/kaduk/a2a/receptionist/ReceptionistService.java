package net.kaduk.a2a.receptionist;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import net.kaduk.a2a.receptionist.model.CapabilityQuery;
import net.kaduk.a2a.receptionist.model.AgentEntity;
import net.kaduk.a2a.receptionist.model.AgentMatchDTO;
import net.kaduk.a2a.receptionist.model.AgentSkillDocument;


@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private final AgentRepositoryCustom agentRepository;
    private final ObjectMapper objectMapper;

    public List<AgentMatchDTO> searchAgentsByCapability(CapabilityQuery query) {
        List<AgentEntity> entities = agentRepository.searchByCapability(query);
        return entities.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private AgentMatchDTO convertToDTO(AgentEntity entity) {
        try {
            AgentSkillDocument doc = objectMapper.readValue(entity.getSkill(), AgentSkillDocument.class);
            return new AgentMatchDTO(
                entity.getName(),
                entity.getUrl(),
                entity.getVersion(),
                doc.getSkills()
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid skill JSON", e);
        }
    }
}
