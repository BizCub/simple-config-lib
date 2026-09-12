package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfig {
    String name();
    Env env() default Env.CLIENT;
    boolean snakeCaseKeys() default false;
    boolean translate() default false;
}
