package org.jpos.annotation.resolvers.response;

import java.lang.reflect.Method;

import org.jpos.annotation.Prepare;
import org.jpos.annotation.Return;

public class VoidContextReturnHandler implements ReturnHandlerProvider {

    @Override
    public boolean isMatch(Method m) {
        return Void.TYPE.equals(m.getReturnType()) && !m.isAnnotationPresent(Return.class);
    }

    @Override
    public ReturnHandler resolve(Method m) {
        return (participant, ctx, res) -> getJPosResult(m);
    }    
}