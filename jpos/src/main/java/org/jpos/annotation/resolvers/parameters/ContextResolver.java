package org.jpos.annotation.resolvers.parameters;

import java.lang.reflect.Parameter;

import org.jpos.annotation.ContextKey;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

public class ContextResolver implements ResolverServiceProvider {

    @Override
    public boolean isMatch(Parameter p) {
        return p.isAnnotationPresent(ContextKey.class);
    }

    @Override
    public Resolver resolve(Parameter p) {
        return new Resolver() {
            String ctxKey;

            @Override
            public void configure(Parameter f) {
                ContextKey annotation = f.getAnnotation(ContextKey.class);
                ctxKey = annotation.value();
            }

            @Override
            public <T> T getValue(TransactionParticipant participant, Context ctx) {
                return ctx.get(ctxKey);
            }
        };
    }
}