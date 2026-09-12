package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Side {
    ConfigSide value();
}
