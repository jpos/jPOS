package org.jpos.util;

/*
 * $Log$
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  2000/01/11 01:25:00  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.1  1999/12/03 13:47:25  apr
 * Added SimpleAntiHog - AntiHog is now an interface
 *
 */

/**
 * prevents Logger from hogging JVM in case of repeated events
 * @since jPOS 1.1
 * @author apr@cs.com.uy
 * @version $Id$
 * @see AntiHog
 */
public class SimpleAntiHog implements AntiHog {
    private long window;
    private int  max;
    long last;
    int  cnt;
    public static final int PENALTY_UNIT = 20;

    /**
     * @param window time window in milliseconds
     * @param max maximun number of calls within window
     */
    public SimpleAntiHog (long window, int max) {
        super();
        this.window = window;
        this.max    = max;
        this.last   = 0;
        this.cnt    = 0;
    }
    private boolean check () {
        long now = System.currentTimeMillis();
        if ((now - window) > last)
            cnt = 0;

        last = now;
        return (cnt++ > max);
    }
    public int nice () {
	int penalty = 0;
        if (check()) {
	    penalty = cnt * PENALTY_UNIT;
            try {
                Thread.sleep (penalty);
            } catch (InterruptedException e) { }
        }
	return penalty;
    }
}
