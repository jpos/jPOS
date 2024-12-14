package org.jpos.annotation.resolvers.response;

import java.lang.reflect.Method;

import org.jpos.annotation.Prepare;
import org.jpos.annotation.PrepareForAbort;
import org.jpos.annotation.resolvers.Priority;

public interface ReturnHandlerProvider extends Priority {
    boolean isMatch(Method m);
    
    ReturnHandler resolve(Method m);
    
    default int getJPosResult(Method m) {
        int jPosRes = 0;
        if (m.isAnnotationPresent(Prepare.class)) {
            jPosRes = m.getAnnotation(Prepare.class).result();
        } else if (m.isAnnotationPresent(PrepareForAbort.class)) {
            jPosRes = m.getAnnotation(PrepareForAbort.class).result();
        }
        return jPosRes;
    }
}