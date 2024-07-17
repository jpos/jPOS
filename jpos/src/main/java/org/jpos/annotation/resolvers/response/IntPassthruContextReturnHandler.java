package org.jpos.annotation.resolvers.response;

import java.lang.reflect.Method;

import org.jpos.annotation.Return;

public class IntPassthruContextReturnHandler implements ReturnHandlerProvider {

    @Override
    public boolean isMatch(Method m) {
        return !m.isAnnotationPresent(Return.class) && int.class.isAssignableFrom(m.getReturnType());
    }

    @Override
    public ReturnHandler resolve(Method m) {
        return (participant, ctx, res) -> (int) res;
    }    
}