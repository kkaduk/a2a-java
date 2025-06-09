// src/main/java/net/kaduk/a2a/A2AAgentRegistry.java
package net.kaduk.a2a;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

@Component
public class A2AAgentRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Holds info about all registered agents:
     * key: bean name, value: AgentMeta (contains metadata, skills)
     */
    @Getter
    private final Map<String, AgentMeta> agentRegistry = new HashMap<>();

    @PostConstruct
    public void scanAgents() {
        // Delay the scanning to avoid circular dependency during startup
        try {
            scanAndRegisterAgents();
        } catch (Exception e) {
            // Log error but don't fail startup
            System.err.println("Error during agent scanning: " + e.getMessage());
        }
    }

    private void scanAndRegisterAgents() {
        Map<String, Object> agentBeans = applicationContext.getBeansWithAnnotation(A2AAgent.class);
        
        for (Map.Entry<String, Object> entry : agentBeans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            
            Class<?> beanClass = bean.getClass();
            A2AAgent agentAnnotation = beanClass.getAnnotation(A2AAgent.class);
            
            if (agentAnnotation != null) {
                List<SkillMeta> skills = new ArrayList<>();

                for (Method method : beanClass.getMethods()) {
                    if (method.isAnnotationPresent(A2AAgentSkill.class)) {
                        A2AAgentSkill skillAnn = method.getAnnotation(A2AAgentSkill.class);
                        skills.add(new SkillMeta(skillAnn, method));
                    }
                }
                
                AgentMeta meta = new AgentMeta(agentAnnotation, bean, skills);
                agentRegistry.put(beanName, meta);
                System.out.println("Registered agent: " + agentAnnotation.name() + " with " + skills.size() + " skills");
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