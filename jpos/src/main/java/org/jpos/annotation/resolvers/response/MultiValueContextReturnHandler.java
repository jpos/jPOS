package org.jpos.annotation.resolvers.response;

import java.lang.reflect.Method;
import java.util.Map;

import org.jpos.annotation.Prepare;
import org.jpos.annotation.Return;

public class MultiValueContextReturnHandler implements ReturnHandlerProvider {
    @Override
    public boolean isMatch(Method m) {        
        if (Map.class.isAssignableFrom(m.getReturnType()) && m.isAnnotationPresent(Return.class)) {
            Return r = m.getAnnotation(Return.class);
            return r.value().length > 1;
       }
        
        return false;
    }

    @Override
    public ReturnHandler resolve(Method m) {
        Return r = m.getAnnotation(Return.class);
        final String[] keys = r.value();
        return (participant, ctx, res) -> {
            Map resMap = (Map) res;
            for(String key: keys) {
                if (resMap != null && resMap.containsKey(key)) {
                    ctx.put(key, resMap.get(key));
                }
            }
            return getJPosResult(m);
        };
    }
}