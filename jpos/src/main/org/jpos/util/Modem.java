/*
 * $Log$
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  2000/01/11 01:24:59  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.1  1999/11/24 18:08:56  apr
 * Added VISA 1 Support
 *
 */

package org.jpos.util;

import java.io.*;

public interface Modem {
    public void dial (String number, long aproxTimeout) throws IOException;
    public void hangup () throws IOException;
    public boolean isConnected();
}
