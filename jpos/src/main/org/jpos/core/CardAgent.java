/*
 * $Log$
 * Revision 1.8  2000/07/19 00:51:23  apr
 * removed cancel method
 *
 * Revision 1.7  2000/06/21 20:43:36  apr
 * Added PersistentEngine support
 *
 * Revision 1.6  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.5  2000/01/30 23:33:49  apr
 * CVS sync/backup - intermediate version
 *
 * Revision 1.4  1999/11/26 12:16:44  apr
 * CVS devel snapshot
 *
 * Revision 1.3  1999/10/08 12:53:56  apr
 * Devel intermediate version - CVS sync
 *
 * Revision 1.2  1999/09/26 22:31:55  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:02  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package org.jpos.core;

import java.io.*;
import java.util.*;
import org.jpos.tpl.PersistentEngine;

/**
 * @author apr@cs.com.uy
 * @version $Id$
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
     * @return true if agent is able to handle this transaction
     */
    public boolean canHandle (CardTransaction t);

    /**
     * create a CardTransactionResponse based on a previously
     * serialized image
     * @param b agent generated image
     * @return CardTransactionResponse
     * @exception CardAgentException
     */
    public CardTransactionResponse getResponse (byte[] b) 
	throws CardAgentException;

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
     * Process a batch of previously completed transactions (close batch)
     * @param l List of pre-aprooved transactions
     * @return List of actually closed transactions
     * @exception CardAgentException
    public List processBatch (List l) throws CardAgentException;
     */

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
