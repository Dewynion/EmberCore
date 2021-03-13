package com.github.Dewynion.embercore.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlSerialized {
    String filename() default "config.yml";
    String path() default "";
    boolean overwriteCollections() default true;
}
