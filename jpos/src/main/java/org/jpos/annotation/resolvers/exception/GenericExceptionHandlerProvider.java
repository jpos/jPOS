package org.jpos.annotation.resolvers.exception;

import java.lang.reflect.Method;

import org.jpos.rc.CMF;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionConstants;
import org.jpos.transaction.TransactionParticipant;

public class GenericExceptionHandlerProvider implements ReturnExceptionHandlerProvider {
    static class GenericExceptionHandler implements ReturnExceptionHandler {

        @Override
        public boolean isMatch(Throwable e) {
            return getException(e, Exception.class) != null;
        }

        @Override
        public int doReturn(TransactionParticipant p, Context ctx, Throwable t) {
            ctx.log("prepare exception in " + this.getClass().getName());
            ctx.log(t);
            setResultCode(ctx, CMF.INTERNAL_ERROR);

            return TransactionConstants.ABORTED;
        }
        
    }

    @Override
    public ReturnExceptionHandler resolve(Method m) {
        return new GenericExceptionHandler();
    }
    
    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
    
}