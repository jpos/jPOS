/**
 * @author apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1999/09/26 19:54:02  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;

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
     * create a CardTransaction based from a previous
     * serialized image
     * @param b transaction image
     * @return CardTransaction
     * @exception IOException
     * @exception ClassNotFoundException
     */
    public CardTransaction create (byte[] b)
	throws IOException, ClassNotFoundException;

    /**
     * @param t promoted CardTransaction
     * @return a serialized image of this transaction
     * @exception IOException;
     */
    public byte[] getImage(CardTransaction t) throws IOException;


    /**
     * @param t promoted CardTransaction
     * @exception CardAgentException
     */
    public void process (CardTransaction t) throws CardAgentException;

    /**
     * @param l List of pre-aprooved transactions
     * @return List of actually closed transactions
     * @exception CardAgentException
     */
    public List processBatch (List l) throws CardAgentException;
}
