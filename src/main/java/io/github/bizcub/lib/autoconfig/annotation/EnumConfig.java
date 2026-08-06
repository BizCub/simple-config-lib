package io.github.bizcub.lib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnumConfig {
    boolean translate() default false;
}
