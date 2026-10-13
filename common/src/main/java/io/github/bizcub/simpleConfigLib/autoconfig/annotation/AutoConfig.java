package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfig {
    String name();
    String fileName() default "";
    ConfigSide side() default ConfigSide.COMMON;
    boolean snakeCaseKeys() default false;
    boolean translate() default false;
}
