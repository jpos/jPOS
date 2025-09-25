package org.jpos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jpos.transaction.TransactionConstants;
import org.jpos.transaction.TransactionParticipant;

/**
 * Indicates that the annotated method is called in the preparation phase of transaction
 * processing, specifying the expected outcome through the {@code result} attribute. 
 * This replaces the {@link TransactionParticipant} prepare method.
 * 
 * @see {@linkplain https://marqeta.atlassian.net/wiki/spaces/~62f54a31d49df231b62a575d/blog/2023/12/01/3041525965/AutoWiring+Participants+with+jPos+-+Part+I}
 * 
 * Usage: Used in conjunction with {@link AnnotatedParticipant} annotations.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PrepareForAbort {
    public int result() default TransactionConstants.PREPARED;
}
