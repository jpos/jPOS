/*
 * $Log$
 * Revision 1.4  2000/04/16 23:53:03  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.3  2000/03/14 00:00:12  apr
 * Added answer method
 *
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  2000/01/11 01:24:56  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.1  1999/11/24 18:08:56  apr
 * Added VISA 1 Support
 *
 */

package org.jpos.util;

import java.io.*;
import javax.comm.*;

public class LeasedLineModem implements Modem {
    V24 v24;

    public LeasedLineModem (V24 v24) {
	super();
	this.v24 = v24;
    }
    
    public void dial (String phoneNumber, long aproxTimeout) 
	throws IOException
    {
    }
    public void answer () throws IOException
    {
    }
    public void hangup () throws IOException {
	throw new IOException ("LeasedLine - cannot hangup");
    }
    public boolean isConnected() {
	return v24.isConnected();
    }
}
