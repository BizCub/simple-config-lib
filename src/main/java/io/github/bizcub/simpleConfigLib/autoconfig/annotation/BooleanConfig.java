package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BooleanConfig {
    boolean yesNo() default true;
}
