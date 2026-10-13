package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Slider {
    int min();
    int max();
    int step() default 1;
}
