package org.jpos.annotation.resolvers.response;

import java.lang.reflect.Method;

import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

public interface ReturnHandler {
    int doReturn(TransactionParticipant p, Context ctx, Object obj);
    
    default void configure(Method m) {}
}
