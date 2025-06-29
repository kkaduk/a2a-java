package net.kaduk.a2a;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "a2a_agents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String version;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;
    
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    @Column
    private boolean active = true;
}