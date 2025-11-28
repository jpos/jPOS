package org.jpos.annotation.resolvers;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.jpos.annotation.resolvers.exception.ReturnExceptionHandlerProvider;
import org.jpos.annotation.resolvers.parameters.ResolverServiceProvider;
import org.jpos.annotation.resolvers.response.ReturnHandlerProvider;

public class ResolverProviderList {
    protected final List<ResolverServiceProvider> resolvers = new ArrayList<>();
    protected final List<ReturnHandlerProvider> resultHandlers = new ArrayList<>();
    protected final List<ReturnExceptionHandlerProvider> exceptionHandlers = new ArrayList<>();

    ResolverProviderList() {
        loadServiceProviders(resolvers, ResolverServiceProvider.class);
        loadServiceProviders(resultHandlers, ReturnHandlerProvider.class);
        loadServiceProviders(exceptionHandlers, ReturnExceptionHandlerProvider.class);
    }
    
    protected <T extends Priority> void loadServiceProviders(List<T> list, Class<T> svcClass) {
        ServiceLoader<T> svcLoader = ServiceLoader.load(svcClass);
        for(T serviceImp: svcLoader) {
            list.add(serviceImp);
        }
        list.sort((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()));
    }
    

    public List<ReturnHandlerProvider> getReturnHandlers() {
        return resultHandlers;
    }
    public List<ReturnExceptionHandlerProvider> getExceptionResolvers() {
        return exceptionHandlers;
    }

    public List<ResolverServiceProvider> getResolvers() {
        return resolvers;
    }
}