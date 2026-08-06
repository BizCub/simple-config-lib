package io.github.bizcub.lib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfig {
    String name();
    boolean snakeCaseKeys() default false;
}
