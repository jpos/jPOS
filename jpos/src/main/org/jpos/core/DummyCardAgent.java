/**
 * @author apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1999/09/26 19:54:06  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;

public class DummyCardAgent implements CardAgent {
    public static final String MYBIN = "999999";

    public int getID() {
	return 0x3b9525d4;
    }
    public void purchase (CardTransaction c) {
	System.out.println ("Paying Transaction");
    }
    public boolean purchaseValidate (CardTransaction t) {
	Map m = t.getMap();
	String rrn = null, termid = null;
	BigDecimal amount = null;

	try {
	    CardHolder ch     = (CardHolder) m.get (CardTransaction.CARDHOLDER);
	    if (MYBIN.equals (ch.getBIN())) {
		amount   = (BigDecimal) m.get (CardTransaction.AMOUNT);
		rrn      = (String)     m.get (CardTransaction.RRN);
		termid   = (String)     m.get (CardTransaction.TERMID);
	    }
	} catch (Exception e) { }
    	return rrn != null && termid != null && amount != null;
    }

    public boolean canHandle (CardTransaction t) {
	Map m = t.getMap();
	String operation = (String) m.get (CardTransaction.OPERATION);
	try {
	    Class[] paramTemplate = { CardTransaction.class };
	    Method method = getClass().getMethod(operation, paramTemplate);
	    method = getClass().getMethod(operation + "Validate", paramTemplate);
	    Object[] param = new Object[1];
	    param[0] = t;
	    return ((Boolean) method.invoke (this, param)).booleanValue();
	} catch (Exception e) { 
	    e.printStackTrace();
	} 
	return false;
    }

    public CardTransaction promote (CardTransaction t) {
	return t;
    }

    public byte[] getImage(CardTransaction t) throws IOException {
	ByteArrayOutputStream b = new ByteArrayOutputStream();
	ObjectOutputStream o = new ObjectOutputStream (b);
	o.writeInt(getID());
	o.writeObject(t.getMap());
	o.flush();
	return b.toByteArray();
    }
    public CardTransaction create (byte[] b)
	throws IOException, ClassNotFoundException
    {
	ByteArrayInputStream i = new ByteArrayInputStream (b);
	ObjectInputStream o    = new ObjectInputStream (i);
	o.readInt();
	Map m = (Map) o.readObject ();
	return new GenericCardTransaction (m);
    }

    public void process (CardTransaction t) {
	Map m = t.getMap();
	String operation = (String) m.get (CardTransaction.OPERATION);
	try {
	    Class[] paramTemplate = { CardTransaction.class };
	    Method method = getClass().getMethod(operation, paramTemplate);
	    Object[] param = new Object[1];
	    param[0] = t;
	    method.invoke (this, param);
	} catch (Exception e) { 
	    e.printStackTrace();
	} 
    }
    public List processBatch (List l)
    {
	return l;
    }
}
