/*
 * $Log$
 * Revision 1.3  2000/01/11 01:24:39  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.2  1999/12/06 01:19:08  apr
 * CVS snapshot
 *
 * Revision 1.1  1999/11/26 12:16:45  apr
 * CVS devel snapshot
 *
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;

import uy.com.cs.jpos.iso.*;
import uy.com.cs.jpos.util.*;

/**
 * @author apr@cs.com.uy
 * @since jPOS 1.1
 * @version $Id$
 */
public abstract class CardAgentBase implements CardAgent, LogProducer {
    protected Sequencer seq;
    protected Configuration cfg;
    protected String realm;
    protected Logger logger;
    protected ISOPackager imagePackager;

    /**
     * @param cfg Configuration provider
     * @param seq Sequencer provider
     * @param logger Logger
     * @param realm  Logger's realm
     */
    public CardAgentBase
	(Configuration cfg, Sequencer seq, Logger logger, String realm)
    {
	this.cfg = cfg;
	this.seq = seq;
	setLogger (logger, realm);
	populateSelector();
	imagePackager = new ISO87BPackager();	// default, can be changed
    }
    public abstract int getID();
    protected abstract void populateSelector();
    public abstract String getPropertyPrefix();

    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
    /**
     * @param packager default internal image packager
     */
    public void setImagePackager (ISOPackager packager) {
	imagePackager = packager;
    }
    /**
     * @return default Image packager
     */
    public ISOPackager getImagePackager() {
	return imagePackager;
    }
    public Configuration getConfiguration() {
	return cfg;
    }
    public boolean canHandle (CardTransaction t) {
	String action = t.getAction();
	try {
	    Class[] paramTemplate = { CardTransaction.class };

	    Method method = getClass().getMethod(action, paramTemplate);
	    method = getClass().getMethod("isValid_" + action, paramTemplate);
	    Object[] param = new Object[1];
	    param[0] = t;
	    return ((Boolean) method.invoke (this, param)).booleanValue();
	} catch (Exception ex) { 
	    Logger.log (new LogEvent (this, "canHandle", ex));
	} 
	return false;
    }

    /**
     * Process CardTransaction
     * @param t CardTransaction
     * @return CardTransactionResponse
     */
    public CardTransactionResponse process (CardTransaction t) 
	throws CardAgentException
    {
	String action = t.getAction();
	try {
	    Class[] paramTemplate = { CardTransaction.class };
	    Method method = getClass().getMethod(action, paramTemplate);
	    Object[] param = new Object[1];
	    param[0] = t;
	    return (CardTransactionResponse) method.invoke (this, param);
	} catch (Exception e) { 
	    Logger.log (new LogEvent (this, "process", e));
	    throw new CardAgentException (e);
	} 
    }

    public abstract CardTransactionResponse getResponse (byte[] b) 
	throws CardAgentException;
}
