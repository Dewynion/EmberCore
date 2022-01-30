package dev.blufantasyonline.embercore.reflection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that something should be loaded or run when a plugin is enabled.
 * Types are instantiated, cached, and treated as singletons; methods are run
 * after all types marked with @OnEnable have been processed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface OnEnable {
}