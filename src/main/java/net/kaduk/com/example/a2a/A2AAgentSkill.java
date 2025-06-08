package com.example.a2a;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface A2AAgentSkill {
    String id();
    String name();
    String description() default "";
    String[] tags() default {};
}
