package org.jpos.core;

import java.io.*;
import java.util.*;
import org.jpos.tpl.PersistentEngine;

/**
 * @author apr@cs.com.uy
 * @version $Revision$ $Date$
 * @since jPOS 1.1
 *
 * Implements financial institution specific functionality<br>
 * CardAgent may rely on <b><i>j</i>POS</b>'s ISO package
 * for the low level interchange implementation.
 */
public interface CardAgent {
    /**
     * @return agent unique ID
     */
    public int getID();

    /**
     * @return Configuration instance
     */
    public Configuration getConfiguration();

    /**
     * @param t CardTransaction
     * @return true if agent is able/willing to handle this transaction
     */
    public boolean canHandle (CardTransaction t);

    /**
     * Process the transaction
     * @param t previously promoted CardTransaction
     * @return CardTransactionInfo object associated with this transaction
     * @exception CardAgentException
     */
    public CardTransactionResponse process (CardTransaction t) 
	throws CardAgentException;

    /**
     * @return property prefix used in configuration
     */
    public String getPropertyPrefix();

    /**
     * Set PersistentEngine associated with this CardAgent
     * @param engine a PersistentEngine instance
     */
    public void setPersistentEngine (PersistentEngine engine);

    /**
     * @return PersistentEngine instance
     */
    public PersistentEngine getPersistentEngine ();

}
