package net.kaduk.a2a.receptionist;

import java.util.List;

import net.kaduk.a2a.receptionist.model.AgentEntity;
import net.kaduk.a2a.receptionist.model.CapabilityQuery;


public interface AgentRepositoryCustom {
    List<AgentEntity> searchByCapability(CapabilityQuery query);
}

