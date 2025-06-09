// src/main/java/net/kaduk/a2a/A2AAutoConfiguration.java
package net.kaduk.a2a;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = "net.kaduk.a2a")
@Import(A2AWebClientConfiguration.class)
public class A2AAutoConfiguration {
}