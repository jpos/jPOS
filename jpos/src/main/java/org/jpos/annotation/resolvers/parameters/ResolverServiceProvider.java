package org.jpos.annotation.resolvers.parameters;

import java.lang.reflect.Parameter;

import org.jpos.annotation.resolvers.Priority;

public interface ResolverServiceProvider extends Priority {
    boolean isMatch(Parameter p);
    Resolver resolve(Parameter p);
}