package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Side {
    Env value();
}
