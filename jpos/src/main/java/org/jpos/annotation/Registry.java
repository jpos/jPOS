package org.jpos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jpos.util.NameRegistrar;

/**
 * Marks a parameter within a method as being from
 * {@link NameRegistrar}
 *
 * @see {@linkplain https://marqeta.atlassian.net/wiki/spaces/~62f54a31d49df231b62a575d/blog/2023/12/01/3041525965/AutoWiring+Participants+with+jPos+-+Part+I}
 * 
 * Usage: Used in conjunction with {@link Prepare} and {@link AnnotatedParticipant} annotations.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Registry {
    public String value() default "";
}
