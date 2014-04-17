package org.jpos.emv;


/**
 * @author Vishnu Pillai
 */
public class NoTagNumberForProprietaryTagException extends RuntimeException {

    public NoTagNumberForProprietaryTagException() {
        super();
    }


    public NoTagNumberForProprietaryTagException(final String message) {
        super(message);
    }


    public NoTagNumberForProprietaryTagException(final String message, final Throwable cause) {
        super(message, cause);
    }


    public NoTagNumberForProprietaryTagException(final Throwable cause) {
        super(cause);
    }
}
