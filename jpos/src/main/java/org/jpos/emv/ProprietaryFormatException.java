package org.jpos.emv;

/**
 * @author Vishnu Pillai
 */
public class ProprietaryFormatException extends Exception {
    public ProprietaryFormatException() {
        super();
    }

    public ProprietaryFormatException(final String message) {
        super(message);
    }

    public ProprietaryFormatException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ProprietaryFormatException(final Throwable cause) {
        super(cause);
    }
}
