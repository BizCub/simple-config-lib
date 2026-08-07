package io.github.bizcub.simpleConfigLib.autoconfig.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ListConfig {
    boolean expanded() default false;
    boolean addToFront() default false;
    boolean editable() default true;
    boolean translateElements() default false;
}
