package net.kaduk.a2a;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;

@Component
public class A2AAgentRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final AgentRepository agentRepository;

    public A2AAgentRegistry(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Getter
    private final Map<String, AgentMeta> agentRegistry = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void registerAgentsOnStartup() {
        Map<String, Object> agentBeans = applicationContext.getBeansWithAnnotation(A2AAgent.class);

        for (Map.Entry<String, Object> entry : agentBeans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();

            Class<?> beanClass = bean.getClass();
            A2AAgent agentAnnotation = beanClass.getAnnotation(A2AAgent.class);

            if (agentAnnotation != null) {
                registerAgent(beanName, bean, agentAnnotation);
            }
        }
    }

    @Transactional
    private void registerAgent(String beanName, Object bean, A2AAgent agentAnnotation) {
        List<SkillMeta> skills = new ArrayList<>();

        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(A2AAgentSkill.class)) {
                A2AAgentSkill skillAnn = method.getAnnotation(A2AAgentSkill.class);
                skills.add(new SkillMeta(skillAnn, method));
            }
        }

        AgentMeta meta = new AgentMeta(agentAnnotation, bean, skills);
        agentRegistry.put(beanName, meta);

        AgentEntity entity = AgentEntity.builder()
                .name(agentAnnotation.name())
                .version(agentAnnotation.version())
                .description(agentAnnotation.description())
                .url(agentAnnotation.url())
                .registeredAt(LocalDateTime.now())
                .lastHeartbeat(LocalDateTime.now())
                .active(true)
                .build();

        Optional<AgentEntity> existing = agentRepository.findByName(agentAnnotation.name());
        if (existing.isPresent()) {
            entity.setId(existing.get().getId());
            entity.setRegisteredAt(existing.get().getRegisteredAt());
        }

        agentRepository.save(entity);
        System.out.println("Registered agent: " + agentAnnotation.name() + " with " + skills.size() + " skills");
    }

    @EventListener(ContextClosedEvent.class)
    @Transactional
    public void deregisterAgentsOnContextClose() {
        for (AgentMeta meta : agentRegistry.values()) {
            agentRepository.deleteByName(meta.getName());
            System.out.println("Deregistered agent: " + meta.getName());
        }
        agentRegistry.clear();
    }

    @Getter
    public static class AgentMeta {
        private final String name;
        private final String version;
        private final String description;
        private final String url;
        private final Object bean;
        private final List<SkillMeta> skills;

        public AgentMeta(A2AAgent agentAnn, Object bean, List<SkillMeta> skills) {
            this.name = agentAnn.name();
            this.version = agentAnn.version();
            this.description = agentAnn.description();
            this.url = agentAnn.url();
            this.bean = bean;
            this.skills = skills;
        }
    }

    @Getter
    public static class SkillMeta {
        private final String id;
        private final String name;
        private final String description;
        private final String[] tags;
        private final Method method;

        public SkillMeta(A2AAgentSkill ann, Method method) {
            this.id = ann.id();
            this.name = ann.name();
            this.description = ann.description();
            this.tags = ann.tags();
            this.method = method;
        }
    }
}