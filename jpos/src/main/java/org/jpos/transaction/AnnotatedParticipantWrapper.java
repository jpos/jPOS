package org.jpos.transaction;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import org.jpos.annotation.Abort;
import org.jpos.annotation.AnnotatedParticipant;
import org.jpos.annotation.Commit;
import org.jpos.annotation.Prepare;
import org.jpos.annotation.PrepareForAbort;
import org.jpos.annotation.resolvers.ResolverFactory;
import org.jpos.annotation.resolvers.exception.ReturnExceptionHandler;
import org.jpos.annotation.resolvers.parameters.Resolver;
import org.jpos.annotation.resolvers.response.ReturnHandler;
import org.jpos.core.ConfigurationException;
import org.jpos.transaction.AnnotatedParticipantWrapper.AnnotatedMethodType;
import org.jpos.transaction.AnnotatedParticipantWrapper.MethodData;
import org.jpos.transaction.AnnotatedParticipantWrapper.TransactionParticipantHandler;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

public class AnnotatedParticipantWrapper {
    enum AnnotatedMethodType {
        PREPARE(Prepare.class, "prepare", "doPrepare"), 
        COMMIT(Commit.class, "commit"), 
        ABORT(Abort.class, "abort"), 
        PREPARE_FOR_ABORT(PrepareForAbort.class, "prepareForAbort");
        
        Class<? extends Annotation> annotation;
        List<String> methodNames = new ArrayList<>();
        
        AnnotatedMethodType(Class<? extends Annotation> annotation, String... methodNames) {
            this.annotation = annotation;
            for (String name : methodNames) {
                this.methodNames.add(name);
            }
        }
                
        Class<? extends Annotation> getAnnotation() {
            return annotation;
        }
    }
    
    class MethodData {
        AnnotatedMethodType type;
        Method method;
        List<Resolver> args = new ArrayList<>();
        ReturnHandler returnHandler;
        List<ReturnExceptionHandler> exceptionHandlers;
        
        public boolean isMatch(Method method) {
            for (String name : type.methodNames) {
                if (method.getName().equals(name) 
                        && method.getParameterCount() == 2 
                        && long.class.isAssignableFrom(method.getParameterTypes()[0])
                        && Serializable.class.isAssignableFrom(method.getParameterTypes()[1])) {
                    return true;
                }
            }
            return false;
        }

        int execute(long id, Serializable o) {
            Context ctx = (Context) o;
            try {
                Object[] resolvedArgs = new Object[args.size()];
                int i = 0;
                for(Resolver r: args) {
                    resolvedArgs[i++] = r.getValue(participant, ctx);
                }
                Object res = method.invoke(participant, resolvedArgs);
                
                return returnHandler.doReturn(participant, ctx, res);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                return processException(ctx, e);
            } catch (InvocationTargetException e) {
                return processException(ctx, e.getTargetException());
            }
        }

        private int processException(Context ctx, Throwable e) {
            ctx.log("Failed to execute " + method.toString());
            ctx.log(e);
            for(ReturnExceptionHandler handler: exceptionHandlers) {
                if (handler.isMatch(e)) {
                    return handler.doReturn(participant, ctx, e);
                }
            }
            throw new RuntimeException(e);
        }
    }

    protected Object participant;
    protected EnumMap<AnnotatedMethodType, MethodData> methods = new EnumMap<>(AnnotatedMethodType.class);
    
    /**
     * Wraps the given {@link TransactionParticipant} object with an {@link AnnotatedParticipantWrapper}, 
     * enabling dynamic method invocation based on annotations. This method is designed to enhance objects
     * marked with {@link AnnotatedParticipant} by providing additional functionalities
     * such as customized prepared method handling.
     * 
     * Note that the return object is a pass thru subclass of the original participant. But while the methods
     * are passed thru to the underlying object, if you access field variables directly, you will be accessing
     * the subclass field variables and not the original participant. For this reason, if the wrapped object is
     * used directly, only access methods.
     *
     * @param obj the object to wrap, typically an instance of a class annotated with
     *            {@link AnnotatedParticipant}.
     * @return an instance of {@link AnnotatedParticipantWrapper} that wraps the provided
     *         object, enriched with dynamic annotation processing capabilities.
     */
    @SuppressWarnings("unchecked")
    public static <T extends TransactionParticipant> T wrap(Object participant) throws ConfigurationException {
        try {
            AnnotatedParticipantWrapper handler = new AnnotatedParticipantWrapper(participant);
            List<Class> interfaces = new ArrayList<>();
            interfaces.add(TransactionParticipant.class);
            if (handler.methods.containsKey(AnnotatedMethodType.PREPARE_FOR_ABORT)) {
                interfaces.add(AbortParticipant.class);
            }
            return (T) new ByteBuddy()
            .subclass(participant.getClass())
            .implement(interfaces.toArray(new Class[0]))
            .method(ElementMatchers.any())
            .intercept(MethodDelegation.to(new TransactionParticipantHandler(handler)))
            .make()
            .load(participant.getClass().getClassLoader())
            .getLoaded()
            .getDeclaredConstructor()
            .newInstance();
        } catch (Throwable e) {
            throw new ConfigurationException("Cound not create the annotated wrapper", e);
        }
    }

    public static boolean isMatch(Object participant) {
        return participant.getClass().isAnnotationPresent(AnnotatedParticipant.class);
    }

    public AnnotatedParticipantWrapper(Object participant) throws ConfigurationException {
        this.participant = participant;
        configureAnnotatedMethods();
    }


    protected List<Resolver> configureParameters(Method m) throws ConfigurationException {
        List<Resolver> args = new ArrayList<>();
        for(Parameter p: m.getParameters()) {
            args.add(ResolverFactory.INSTANCE.getResolver(p));
        }
        return args;
    }

    protected void configureAnnotatedMethods() throws ConfigurationException {
        for(Method m: participant.getClass().getMethods()) {
            for (AnnotatedMethodType type : AnnotatedMethodType.values()) {
                if (m.isAnnotationPresent(type.getAnnotation())) {
                    if (methods.containsKey(type)) {
                        throw new ConfigurationException("Only one method per class can be defined with the @"
                                + type.getAnnotation().getSimpleName() + ". " + participant.getClass().getSimpleName()
                                + " has multiple matches.");
                    }
                    MethodData data = new MethodData();
                    data.type = type;
                    data.method = m;
                    data.exceptionHandlers = ResolverFactory.INSTANCE.getExceptionHandlers(m);
                    data.returnHandler = ResolverFactory.INSTANCE.getReturnHandler(m);
                    data.args = configureParameters(m);
                    methods.put(type, data);
                }
            }
        }
        
        if (methods.isEmpty()) {
            throw new ConfigurationException(participant.getClass().getSimpleName() + " needs one method defined with the @Prepare annotation.");            
        }
        if (!(participant instanceof TransactionParticipant) &&
                !methods.keySet().containsAll(
                        Arrays.asList(AnnotatedMethodType.PREPARE, AnnotatedMethodType.COMMIT, AnnotatedMethodType.ABORT))) {
            throw new ConfigurationException(
                    participant.getClass().getSimpleName() + " needs to define all of the @Prepare, @Commit, and @Abort annotations or implement TransactionParticipant.");
        }
    }
    
    public static class TransactionParticipantHandler {
        
        AnnotatedParticipantWrapper parent;
        
        private TransactionParticipantHandler(AnnotatedParticipantWrapper parent) {
            this.parent = parent;
        }
        
        @RuntimeType
        public Object intercept(@Origin Method method, @AllArguments @RuntimeType Object[] args) 
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
            for (MethodData data : parent.methods.values()) {
                if (data.isMatch(method)) {
                    int res = data.execute((long) args[0], (Serializable) args[1]);
                    if (method.getReturnType().equals(void.class)) {
                        return null;
                    } else {
                        return res;
                    }
                }
            }
            return method.invoke(parent.participant, args);
        }
    }
}


