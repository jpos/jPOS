/*
 * $Log$
 * Revision 1.5  2000/03/02 12:31:01  apr
 * Get rid of javadoc warnings - done
 *
 * Revision 1.4  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.3  2000/01/26 21:48:49  apr
 * CVS sync
 *
 * Revision 1.2  2000/01/20 23:02:45  apr
 * Adding FinancialTransaction support - CVS sync
 *
 * Revision 1.1  1999/11/26 12:16:48  apr
 * CVS devel snapshot
 *
 */


package org.jpos.core;

import java.io.*;
import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * @see CardTransaction
 * @see ErrorResponse
 */
public interface CardTransactionResponse extends Serializable {
    /**
     * provides a [signed] [encripted] serialized image of a given
     * previously processed transaction 
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
    public String  getBatchName();
}
