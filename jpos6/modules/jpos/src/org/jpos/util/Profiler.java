/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

package org.jpos.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Iterator;

/**
 * Simple Profiler
 * @author Alejandro P. Revilla
 * @author David D. Bergert
 * @version $Id$
 */
public class Profiler implements Loggeable {
    long start, parcial;
    LinkedHashMap events;
    public Profiler () {
        super();
        reset();
    }
    /**
     * reset timers
     */
    public void reset() {
        start = parcial = System.currentTimeMillis();
        events = new LinkedHashMap();
    }
    /**
     * mark checkpoint
     * @param detail checkpoint information
     */
    public synchronized void checkPoint (String detail) {
        long now = System.currentTimeMillis();
        Entry e = new Entry();
        e.setEventName(detail);
        e.setDuration(now - parcial);
        e.setTotalDuration(now - start);
        events.put (detail, e);
        parcial = now;
    }
    /**
     * @return total elapsed time since last reset
     */
    public long getElapsed() {
        return System.currentTimeMillis() - start;
    }
    /**
     * @return parcial elapsed time since last reset
     */
    public long getParcial() {
        return System.currentTimeMillis() - parcial;
    }
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        parcial = start;
        checkPoint ("end");
        Collection c = events.values();
        Iterator iter = c.iterator();
        p.println (indent + "<profiler>");
        while (iter.hasNext()) 
            p.println (inner + ((Entry) iter.next()).toString());
        p.println (indent + "</profiler>");
    }
    public Entry getEntry(String eventName) {
         return (Entry)events.get(eventName);         
    }
    public class Entry  {     
        String  eventName;
        long    duration;
        long    totalDuration;          
        Entry(){     
           eventName     = "";
           duration      = 0L;
           totalDuration = 0L;        
        }  
       public void setEventName (String myEvent) {
            this.eventName = myEvent;
        }
        String getEventName () {
            return eventName;
        }    
        public void setDuration (long myDuration) {
            this.duration = myDuration;
        }
        public long getDuration () {
            return duration;
        }    
        public void setTotalDuration (long myTotalDuration) {
            this.totalDuration = myTotalDuration;
        }
        public long getTotalDuration () {
            return totalDuration;
        }    
        public String toString()  {
            StringBuffer sb = new StringBuffer (eventName);
            sb.append (" [");
            sb.append (duration);
            sb.append ('/');
            sb.append (totalDuration);
            sb.append (']');
            return sb.toString();
        }            
    }
}