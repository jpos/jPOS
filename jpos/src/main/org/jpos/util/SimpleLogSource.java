package org.jpos.util;

/*
 * $Log$
 * Revision 1.1  2000/04/16 23:53:14  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  2000/01/11 01:25:02  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.2  1999/12/20 00:56:46  apr
 * Added public constructors
 *
 * Revision 1.1  1999/11/24 18:03:28  apr
 * added to repository
 *
 */

/**
 * LogSources can choose to extends this SimpleLogSource
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see LogSource
 */
public class SimpleLogSource implements LogSource {
    protected Logger logger;
    protected String realm;

    public SimpleLogSource () {
	super();
	logger = null;
	realm  = null;
    }
    public SimpleLogSource (Logger logger, String realm) {
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
