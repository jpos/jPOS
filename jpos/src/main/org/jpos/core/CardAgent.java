/*
 * $Log$
 * Revision 1.2  1999/09/26 22:31:55  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:02  apr
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
     * @param t Generic CardTransaction
     * @return true if agent is able to handle this transaction
     */
    public boolean canHandle (CardTransaction t);

    /**
     * Factory Method pattern - create a specialized CardTransaction
     * based on a generic one
     * @param t Generic CardTransaction
     * @return specialized CardTransaction
     */
    public CardTransaction promote (CardTransaction t);

    /**
     * create a CardTransaction based on a previously serialized image
     * @param b transaction image
     * @return CardTransaction
     * @exception IOException
     * @exception ClassNotFoundException
     */
    public CardTransaction create (byte[] b)
	throws IOException, ClassNotFoundException;

    /**
     * provides a [signed] [encripted] serialized image of a given
     * previously promoted and processed transaction 
     * (suitable to be saved on persistent storage)
     * @param t specific CardTransaction
     * @return a serialized image of this transaction
     * @exception IOException if problems arise during serialization
     * @exception CardAgentException if not ready for Serialization
     */
    public byte[] getImage(CardTransaction t)
	throws IOException, CardAgentException;


    /**
     * Process the transaction
     * @param t previously promoted CardTransaction
     * @exception CardAgentException
     */
    public void process (CardTransaction t) throws CardAgentException;

    /**
     * Process a batch of previously completed transactions (close batch)
     * @param l List of pre-aprooved transactions
     * @return List of actually closed transactions
     * @exception CardAgentException
     */
    public List processBatch (List l) throws CardAgentException;
}
