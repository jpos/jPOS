package org.jpos.annotation.resolvers.parameters;

import java.lang.reflect.Parameter;

import org.jpos.core.ConfigurationException;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

public interface Resolver {
    default void configure(Parameter f) throws ConfigurationException {
    }
    <T> T getValue(Object participant, Context ctx);
}
