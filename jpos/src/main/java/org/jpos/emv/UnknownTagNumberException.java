package org.jpos.emv;

/**
 * @author Vishnu Pillai
 */
public class UnknownTagNumberException extends Exception {

    public UnknownTagNumberException() {
        super();
    }

    public UnknownTagNumberException(final String message) {
        super(message);
    }

    public UnknownTagNumberException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnknownTagNumberException(final Throwable cause) {
        super(cause);
    }
}
