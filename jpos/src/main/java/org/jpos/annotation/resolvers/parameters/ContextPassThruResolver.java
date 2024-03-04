package org.jpos.annotation.resolvers.parameters;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jpos.annotation.ContextKeys;
import org.jpos.core.ConfigurationException;
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
        if ( p.isAnnotationPresent(ContextKeys.class)) {
         return new Resolver() {
             Set<String> readWrite = new HashSet<>();
             Set<String> readOnly = new HashSet<>();
             Set<String> writeOnly = new HashSet<>();
             @Override
            public void configure(Parameter f) throws ConfigurationException {
                 ContextKeys annotation = f.getAnnotation(ContextKeys.class);
                 readWrite.addAll(Arrays.asList(annotation.value()));
                 readOnly.addAll(Arrays.asList(annotation.read()));
                 writeOnly.addAll(Arrays.asList(annotation.write()));
                 if (readOnly.isEmpty() && writeOnly.isEmpty() && readWrite.isEmpty()) {
                     throw new ConfigurationException("At least one key for read or write has to be defined.");
                 }
            }
            @Override
            public <T> T getValue(TransactionParticipant participant, Context ctx) {
                return (T) new ContextView(ctx, readWrite, readOnly, writeOnly);
            }
             
         };
        } else {
            return new Resolver() {
                @Override
                public <T> T getValue(TransactionParticipant participant, Context ctx) {
                    return (T) ctx;
                }
            };
        }
    }
}