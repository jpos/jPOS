package org.jpos.util;

/*
 * $Log$
 * Revision 1.3  2000/04/16 23:53:14  apr
 * LogProducer renamed to LogSource
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
 * Revision 1.2  1999/12/03 13:47:23  apr
 * Added SimpleAntiHog - AntiHog is now an interface
 *
 * Revision 1.1  1999/12/01 18:19:36  apr
 * Added AntiHog support
 *
 */

/**
 * prevents Logger from hogging JVM in case of repeated events
 * @since jPOS 1.1
 * @author apr@cs.com.uy
 * @version $Id$
 * @see Logger
 */
public interface AntiHog {
    /*
     * @return penalty in milliseconds
     */
    public int nice ();
}
