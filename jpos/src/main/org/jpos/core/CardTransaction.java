/**
 */

/*
 * $Log$
 * Revision 1.2  1999/09/26 22:31:58  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:06  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;


/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * Application should create a template CardTransaction 
 * (i.e. an instance of GenericCardTransaction), find a
 * suitable agent (by means of CardAgentLookup singleton) 
 * and use that agent factory method <b>promote</b>
 * in order to create a specialized CardTransaction.
 * <p>
 * Although a CardTransaction can hold any number of predefined 
 * properties or none at all (that's defined by the agent in use)
 * we define here a few handy constants for well known input/output
 * properties
 *
 * @see CardAgent
 * @see CardAgentLookup
 * @see CardHolder
 */
public interface CardTransaction {
    /**
     * a CardHolder Object 
     * @see CardHolder
     */
    public static final String CARDHOLDER = "card";

    /**
     * Unique Retrieval Reference Number for this transaction
     * (from high level application point of view)
     */
    public static final String RRN        = "rrn";

    /**
     * Amount for this transaction
     */
    public static final String AMOUNT     = "amount";

    /**
     * Local Terminal ID.<br>
     * This may or may not be the Terminal ID assigned by acquiring
     * institution. CardAgent may map this Terminal ID to another
     * number (same goes for Merchant Number)
     */
    public static final String TERMID     = "termd";

    /**
     * Operation to be done on this transaction
     */
    public static final String OPERATION  = "oper";


    /**
     * PURCHASE Operation
     */
    public static final String OPER_PURCHASE = "purchase";

    /**
     * CANCEL Operation
     */
    public static final String OPER_CANCEL = "cancel";

    /**
     * REFUND Operation
     */
    public static final String OPER_REFUND = "refund";


    /**
     * Authorization Code
     */
    public static final String AUT_CODE= "autcode";

    /**
     * Authorization Number
     */
    public static final String AUT_NUMBER  = "autnumber";

    /**
     * Authorization Message<br>
     * (short - suitable for POS, usually less than 18 chars)
     */
    public static final String AUT_SHORT_MESSAGE = "autmsg";

    /**
     * Authorization Description
     */
    public static final String AUT_MESSAGE = "autdesc";

    /**
     * @return CardTransaction properties
     */
    public Map getMap();


    /**
     * @return true if aproved
     */
    public boolean isAproved();

    /**
     * Transaction requires operator attention<br>
     * (i.e. having an operator call an authorization centre)
     * but still can evolve to a Completed transaction.<br>
     *
     * @return true if transaction can proceed (N/A if already completed)
     */
    public boolean canContinue();

    /**
     * dump transaction
     * @param p a PrintStream
     * @param indent how much to indent output
     */
    public void dump (PrintStream p, String indent);
}
