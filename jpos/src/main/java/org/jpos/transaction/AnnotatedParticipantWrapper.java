package org.jpos.transaction;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.jpos.annotation.AnnotatedParticipant;
import org.jpos.annotation.Prepare;
import org.jpos.annotation.resolvers.ResolverFactory;
import org.jpos.annotation.resolvers.exception.ReturnExceptionHandler;
import org.jpos.annotation.resolvers.parameters.Resolver;
import org.jpos.annotation.resolvers.response.ReturnHandler;
import org.jpos.core.ConfigurationException;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

public class AnnotatedParticipantWrapper {

    protected TransactionParticipant participant;
    protected Method prepare;
    
    List<Resolver> args = new ArrayList<>();
    ReturnHandler returnHandler;
    List<ReturnExceptionHandler> exceptionHandlers;
    Method checkPoint;

    public static <T extends TransactionParticipant> T wrap(T participant) throws ConfigurationException {
        try {
            AnnotatedParticipantWrapper handler = new AnnotatedParticipantWrapper(participant);
            return (T) new ByteBuddy()
            .subclass(participant.getClass())
            .method(ElementMatchers.any())
            .intercept(MethodDelegation.to(handler))
            .make()
            .load(participant.getClass().getClassLoader())
            .getLoaded()
            .getDeclaredConstructor()
            .newInstance();
        } catch (Throwable e) {
            throw new ConfigurationException("Cound not create the annotated wrapper", e);
        }
    }

    public static boolean isMatch(TransactionParticipant participant) {
        return participant.getClass().isAnnotationPresent(AnnotatedParticipant.class);
    }

    public AnnotatedParticipantWrapper(TransactionParticipant participant) throws ConfigurationException {
        this.participant = participant;
        configurePrepareMethod();
        configureReturnHandler();
        configureParameters();
        
    }

    protected void configureReturnHandler() throws ConfigurationException {
        returnHandler = ResolverFactory.INSTANCE.getReturnHandler(prepare);
        exceptionHandlers = ResolverFactory.INSTANCE.getExceptionHandlers(prepare);
    }

    protected void configureParameters() throws ConfigurationException {
        for(Parameter p: prepare.getParameters()) {
            args.add(ResolverFactory.INSTANCE.getResolver(p));
        }
    }

    protected void configurePrepareMethod() throws ConfigurationException {
        for(Method m: participant.getClass().getMethods()) {
            if (m.isAnnotationPresent(Prepare.class)) {
                if (prepare == null) {
                    prepare = m;
                } else {
                    throw new ConfigurationException("Only one method per class can be defined with the @Prepare. " + participant.getClass().getSimpleName() + " has multiple matches.");
                }
            }
        }
        if (prepare == null) {
            throw new ConfigurationException(participant.getClass().getSimpleName() + " needs one method defined with the @Prepare annotation.");            
        }
    }


    public final int prepare(long id, Serializable o) {
        Context ctx = (Context) o;
        try {
            Object[] resolvedArgs = new Object[args.size()];
            int i = 0;
            for(Resolver r: args) {
                resolvedArgs[i++] = r.getValue(participant, ctx);
            }
            Object res = prepare.invoke(participant, resolvedArgs);
            
            return returnHandler.doReturn(participant, ctx, res);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            return processException(ctx, e);
        } catch (InvocationTargetException e) {
            return processException(ctx, e.getTargetException());
        }
    }

    private int processException(Context ctx, Throwable e) {
        ctx.log("Failed to execute " + prepare.toString());
        ctx.log(e);
        for(ReturnExceptionHandler handler: exceptionHandlers) {
            if (handler.isMatch(e)) {
                return handler.doReturn(participant, ctx, e);
            }
        }
        throw new RuntimeException(e);
    }

    @RuntimeType
    public Object intercept(@AllArguments Object[] args,
                            @Origin Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
        return method.invoke(participant, args);
    }
}


