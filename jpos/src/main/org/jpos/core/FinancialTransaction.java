/*
 * $Log$
 * Revision 1.1  2000/01/20 23:02:46  apr
 * Adding FinancialTransaction support - CVS sync
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.math.*;
import java.util.*;
import uy.com.cs.jpos.util.Loggeable;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * @see CardTransaction
 * @see CardAgentLookup
 * @see CardHolder
 */
public class FinancialTransaction extends AuthorizationTransaction
{
    private transient CardTransactionResponse response;
    private transient BatchManager batchManager;

    public FinancialTransaction() {
	super();
    }
    public FinancialTransaction(BatchManager batchManager, String action) {
	setBatchManager (batchManager);
	setAction (action);
    }
    public void setBatchManager (BatchManager batchManager) {
	this.batchManager = batchManager;
    }
    public void dump (PrintStream p, String indent) {
	String inner = indent + "  ";
	p.println (indent + "<FinancialTransaction>");
	dump0 (p, inner);
	if (response != null && response instanceof Loggeable)
	    ((Loggeable)response).dump (p, inner);
	p.println (indent + "</FinancialTransaction>");
    }
    public void setResponse (CardTransactionResponse response) {
	this.response = response;
    }
    public CardTransactionResponse getResponse () {
	return response;
    }
    public void sync () throws IOException {
	if (batchManager == null)
	    throw new IOException ("batch manager not available");
	batchManager.sync (this);
    }
}
