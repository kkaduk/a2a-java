package net.kaduk.a2a;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class A2AAgentSkillDTO {
    private String id;
    private String name;
    private String description;
    private List<String> tags;  
}