package org.jpos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jpos.transaction.Context;

/**
 * Used to specify a key for retrieving contextual information from a {@link Context} object via
 * the annotated parameter. This facilitates dynamic access to context-specific data,
 * streamlining the process of working with application contexts.
 * 
 * value are in/out keys
 * read only allow for read only
 * write only allows for writes
 *
 * @see {@linkplain https://marqeta.atlassian.net/wiki/spaces/~62f54a31d49df231b62a575d/blog/2023/12/01/3041525965/AutoWiring+Participants+with+jPos+-+Part+I}
 * 
 * Usage: Used in conjunction with {@link Prepare} and {@link AnnotatedParticipant} annotations.
 * 
 *
 * @author Ozzy Espaillat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ContextKeys {
    public String[] value() default {};
    public String[] write() default {};
    public String[] read() default {};
}
