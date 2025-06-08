package com.example.a2a;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface A2AAgent {
    String name();
    String version();
    String description() default "";
}
