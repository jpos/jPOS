package org.jpos.core;

public interface LUHNCalculator {
    boolean verify (String pan) throws InvalidCardException;
    char calculate (String pan) throws InvalidCardException;
}
