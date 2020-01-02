/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * A simple sequencer intended for Debugging applications.<br>
 * Production grade Sequencers are required to be persistent capables
 */
@SuppressWarnings("unchecked")
public class VolatileSequencer implements Sequencer, VolatileSequencerMBean {
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
            i = I;
        i += add;
        map.put (counterName, i);
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
            oldValue = I;
        map.put (counterName, newValue);
        return oldValue;
    }
    public String[] getCounterNames () {
        Object[] o = map.keySet().toArray();
        String[] s = new String [o.length];
        System.arraycopy (o, 0, s, 0, o.length);
        return s;
    }
}
