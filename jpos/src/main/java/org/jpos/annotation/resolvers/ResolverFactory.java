package org.jpos.annotation.resolvers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.jpos.annotation.resolvers.exception.ReturnExceptionHandler;
import org.jpos.annotation.resolvers.exception.ReturnExceptionHandlerProvider;
import org.jpos.annotation.resolvers.parameters.Resolver;
import org.jpos.annotation.resolvers.parameters.ResolverServiceProvider;
import org.jpos.annotation.resolvers.response.ReturnHandler;
import org.jpos.annotation.resolvers.response.ReturnHandlerProvider;
import org.jpos.core.ConfigurationException;

public class ResolverFactory {
    public static final ResolverFactory INSTANCE = new ResolverFactory();
    
    protected final ResolverProviderList resolvers = new ResolverProviderList();
    
    private ResolverFactory() {}
    
    public Resolver getResolver(Parameter p) throws ConfigurationException {
        Resolver r = null;
        for (ResolverServiceProvider f: resolvers.getResolvers()) {
            if (f.isMatch(p)) {
                r = f.resolve(p);
                r.configure(p);
                break;
            }
        }
        if (r == null) {
            throw new ConfigurationException("Prepare parameter " + p.getName() + " does not have the required annotation.");                            
        }
        return r;
    }
    
    public ReturnHandler getReturnHandler(Method m) throws ConfigurationException {
        ReturnHandler r = null;
        for (ReturnHandlerProvider f: resolvers.getReturnHandlers()) {
            if (f.isMatch(m)) {
                r = f.resolve(m);
                r.configure(m);
                break;
            }
        }
        if (r == null) {
            throw new ConfigurationException("Could not find a valid provider for return " + m.getName());                            
        }
        return r;
    }
    
    public List<ReturnExceptionHandler> getExceptionHandlers(Method m) {
        List<ReturnExceptionHandler> exceptionHandlers = new ArrayList<>();
        for(ReturnExceptionHandlerProvider p: resolvers.getExceptionResolvers()) {
            ReturnExceptionHandler r = p.resolve(m);
            r.configure(m);
            exceptionHandlers.add(r);
        }
        return exceptionHandlers;
    }

}