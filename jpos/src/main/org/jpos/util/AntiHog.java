package uy.com.cs.jpos.util;

/*
 * $Log$
 * Revision 1.1  2000/01/11 01:24:56  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
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
