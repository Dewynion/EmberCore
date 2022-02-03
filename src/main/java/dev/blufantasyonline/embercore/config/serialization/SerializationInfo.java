package dev.blufantasyonline.embercore.config.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializationInfo {
    /**
     * Please use {@link #location()} instead.
     */
    @Deprecated String filename() default "";

    /**
     * Specifies the location to read from.
     *
     * If applied to a type, sets this as the default location for all serialized fields.
     */
    String location() default "";

    boolean remote() default false;

    /**
     * Specifies the path through the config tree taken to reach the value for a field.
     * When applied to a type, this value will be prepended to all paths in the file.
     */
    String path() default "";
}
