/**
 * @author apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1999/09/26 19:54:07  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;
import java.math.*;
import uy.com.cs.jpos.iso.ISOUtil;

public class Test {
    public Test() {
	super();
    }
    public void registerAgents() {
	CardAgentLookup.add (new DummyCardAgent());
    }
    public void testPurchase 
	(String rrn, String pan, String exp, BigDecimal amount)
	throws InvalidCardException, CardAgentNotFoundException,
		IOException, ClassNotFoundException, CardAgentException
    
    {
	CardTransaction t = new GenericCardTransaction();
	Map m  = t.getMap();
	m.put (CardTransaction.OPERATION, "purchase");
	m.put (CardTransaction.RRN, rrn);
	m.put (CardTransaction.TERMID, "00000001");
	m.put (CardTransaction.CARDHOLDER, new CardHolder (pan, exp));
	m.put (CardTransaction.AMOUNT, amount);

	// find an agent willing to process this transaction
	CardAgent agent = CardAgentLookup.getAgent (t);

	// use Factory Method pattern to promote the transaction
	t = agent.promote (t);

	// get and an image of our transaction
	byte[] transactionImage = agent.getImage(t);

	//
	// at this point we should save transactionImage on persistent storage
	//


	//
	// Request Agent to process the transaction
	//
	agent.process (t);

	t.dump (System.out, "");


	System.out.println ("testPurchase OK - agent "+ agent);
	CardAgent anotherAgent = CardAgentLookup.getAgent (transactionImage);
	System.out.println ("another Agent "+ anotherAgent);
    }

    
    public static void main (String args[]) {
	Test t = new Test();
	try {
	    t.registerAgents();
	    t.testPurchase( 
		"000001", "9999990000000001", "0001", new BigDecimal("100.00"));
	}
	catch (Throwable e) {
	    e.printStackTrace();
	}
    }
}
