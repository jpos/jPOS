package uy.com.cs.jpos.util;

/*
 * $Log$
 * Revision 1.1  2000/01/11 01:25:02  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.2  1999/12/20 00:56:46  apr
 * Added public constructors
 *
 * Revision 1.1  1999/11/24 18:03:28  apr
 * added to repository
 *
 */

/**
 * LogProducers can choose to extends this SimpleLogProducer
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see LogProducer
 */
public class SimpleLogProducer implements LogProducer {
    protected Logger logger;
    protected String realm;

    public SimpleLogProducer () {
	super();
	logger = null;
	realm  = null;
    }
    public SimpleLogProducer (Logger logger, String realm) {
	setLogger (logger, realm);
    }
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
}
