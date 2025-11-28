package org.jpos.annotation.resolvers.exception;

import java.lang.reflect.Method;

import org.jpos.annotation.resolvers.Priority;

public interface ReturnExceptionHandlerProvider extends Priority {
    ReturnExceptionHandler resolve(Method m);    
}