package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfig {
    String name();
    Side.Env env() default Side.Env.CLIENT;
    boolean snakeCaseKeys() default false;
    boolean translate() default false;
}
