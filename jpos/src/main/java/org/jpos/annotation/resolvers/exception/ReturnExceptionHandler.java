package org.jpos.annotation.resolvers.exception;

import java.lang.reflect.Method;

import org.jpos.rc.IRC;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

public interface ReturnExceptionHandler {
    boolean isMatch(Throwable e);
    int doReturn(TransactionParticipant p, Context ctx, Throwable obj);

    default void configure(Method m) {}
    
    default void setResultCode(Context ctx, IRC irc) {
        ctx.put(ContextConstants.IRC, irc);
    }
    
    default <T> T getException(Throwable e, Class<T> type) {
        int stackDepth= 10;
        do {
            if (type.isAssignableFrom(e.getClass())) return (T) e;
        } while (null != (e = e.getCause()) && stackDepth-- > 0);
        return null;
    }
}