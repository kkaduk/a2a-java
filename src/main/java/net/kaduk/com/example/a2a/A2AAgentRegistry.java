package com.example.a2a;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class A2AAgentRegistry {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Holds info about all registered agents:
     * key: bean name, value: AgentMeta (contains metadata, skills)
     */
    @Getter
    private final Map<String, AgentMeta> agentRegistry = new HashMap<>();

    @PostConstruct
    public void scanAgents() throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);

            Class<?> beanClass = bean.getClass();
            if (beanClass.isAnnotationPresent(A2AAgent.class)) {
                A2AAgent agentAnnotation = beanClass.getAnnotation(A2AAgent.class);
                List<SkillMeta> skills = new ArrayList<>();

                for (Method method : beanClass.getMethods()) {
                    if (method.isAnnotationPresent(A2AAgentSkill.class)) {
                        A2AAgentSkill skillAnn = method.getAnnotation(A2AAgentSkill.class);
                        skills.add(new SkillMeta(skillAnn, method));
                    }
                }
                AgentMeta meta = new AgentMeta(agentAnnotation, bean, skills);
                agentRegistry.put(beanName, meta);
            }
        }
    }

    @Getter
    public static class AgentMeta {
        private final String name;
        private final String version;
        private final String description;
        private final Object bean;
        private final List<SkillMeta> skills;

        public AgentMeta(A2AAgent agentAnn, Object bean, List<SkillMeta> skills) {
            this.name = agentAnn.name();
            this.version = agentAnn.version();
            this.description = agentAnn.description();
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
