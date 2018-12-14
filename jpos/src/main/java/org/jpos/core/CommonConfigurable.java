package org.jpos.core;

public interface CommonConfigurable {
    default void runPostConfiguration() throws ConfigurationException { }
}
