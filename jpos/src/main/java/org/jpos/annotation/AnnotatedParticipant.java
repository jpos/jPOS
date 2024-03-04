package org.jpos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jpos.transaction.TransactionParticipant;

/**
 * Marks a {@link TransactionParticipant} a participant defined using annotations, automatically
 * binding the prepare method and parameters. This annotation is  used
 * in convention-over-configuration scenarios to simplify the integration of components and
 * make testing easier.
 *
 * see {@linkplain https://marqeta.atlassian.net/wiki/spaces/~62f54a31d49df231b62a575d/blog/2023/12/01/3041525965/AutoWiring+Participants+with+jPos+-+Part+I}
 * 
 * Usage: Apply on classes extending {@link TransactionParticipant} and implementing a {@link Prepare} method instead of the prepare(long, Serializable).
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnotatedParticipant{

}
