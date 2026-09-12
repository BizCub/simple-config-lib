package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfig {
    String name();
    String fileName() default "";
    Env env() default Env.COMMON;
    boolean snakeCaseKeys() default false;
    boolean translate() default false;
}
