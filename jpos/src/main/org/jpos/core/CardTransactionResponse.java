/*
 * $Log$
 * Revision 1.1  1999/11/26 12:16:48  apr
 * CVS devel snapshot
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
 * @see CardTransaction, ErrorResponse
 */
public interface CardTransactionResponse extends Serializable {
    /**
     * provides a [signed] [encripted] serialized image of a given
     * previously promoted and processed transaction 
     * (suitable to be saved on persistent storage)
     * @return a serialized image of this transaction
     */
    public byte[] getImage() throws CardAgentException;

    public String  getAutCode();
    public String  getMessage();
    public String  getAutNumber();
    public boolean isApproved();
    public boolean canContinue();
    public boolean isAuthoritative();
}
