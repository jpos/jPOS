package org.jpos.annotation.resolvers.parameters;

import java.lang.reflect.Parameter;

import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

public class ContextPassThruResolver implements ResolverServiceProvider {

    @Override
    public boolean isMatch(Parameter p) {
        return Context.class.isAssignableFrom(p.getType());
    }
    
    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public Resolver resolve(Parameter p) {
        return new Resolver() {
            @Override
            public <T> T getValue(TransactionParticipant participant, Context ctx) {
                return (T) ctx;
            }
        };
    }
}