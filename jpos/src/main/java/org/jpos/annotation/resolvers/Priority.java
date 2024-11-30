package org.jpos.annotation.resolvers;

public interface Priority {    
    default int getPriority() {
        return 10;
    }
}