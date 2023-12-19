package org.jpos.annotation.resolvers.response;

import java.lang.reflect.Method;

import org.jpos.annotation.Prepare;
import org.jpos.annotation.Return;

public class SingleValueContextReturnHandler implements ReturnHandlerProvider {
    @Override
    public boolean isMatch(Method m) {        
        if (m.isAnnotationPresent(Return.class) && !Void.TYPE.equals(m.getReturnType())) {
            Return r = m.getAnnotation(Return.class);
            return r.value().length == 1;
       }
        
        return false;
    }

    @Override
    public ReturnHandler resolve(Method m) {
        Return r = m.getAnnotation(Return.class);
        final String key = r.value()[0];
        final int jPosRes = m.getAnnotation(Prepare.class).result();
        return (participant, ctx, res) -> {
            if (res != null) {
                ctx.put(key, res);
            }
            return jPosRes;
        };
    }
}