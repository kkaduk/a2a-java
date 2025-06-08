package net.kaduk.a2a;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the a2a-java library.
 * Ensures all components in 'net.kaduk.a2a' are registered when used as a dependency.
 */
@Configuration
@ComponentScan(basePackages = "net.kaduk.a2a")
public class A2AAutoConfiguration {
    // No additional configuration needed; beans picked up via component scan.
}
