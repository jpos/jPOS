
/*
 * $Log$
 * Revision 1.2  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  1999/09/26 22:32:03  apr
 * CVS sync
 *
 */

package org.jpos.core;

import java.io.*;
import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * A simple sequencer intended for Debugging applications.<br>
 * Production grade Sequencers are required to be persistent capables
 */
public class VolatileSequencer implements Sequencer {
    private Map map;
    public VolatileSequencer () {
	map = new HashMap();
    }
    /**
     * @param counterName
     * @param add increment
     * @return counterName's value + add
     */
    synchronized public int get (String counterName, int add) {
	int i = 0;
	Integer I = (Integer) map.get (counterName);
	if (I != null)
	    i = I.intValue();
	i += add;
	map.put (counterName, new Integer (i));
	return i;
    }
    /**
     * @param counterName
     * @return counterName's value + 1
     */
    public int get (String counterName) {
	return get (counterName, 1);
    }
    /**
     * @param counterName
     * @param newValue
     * @return oldValue
     */
    synchronized public int set (String counterName, int newValue) {
	int oldValue = 0;
	Integer I = (Integer) map.get (counterName);
	if (I != null)
	    oldValue = I.intValue();
	map.put (counterName, new Integer (newValue));
	return oldValue;
    }
}
