package org.jpos.annotation.resolvers.response;

import java.lang.reflect.Method;

import org.jpos.annotation.resolvers.Priority;

public interface ReturnHandlerProvider extends Priority {
    boolean isMatch(Method m);
    ReturnHandler resolve(Method m);
}