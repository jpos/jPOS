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

package org.jpos.util;

import org.jpos.iso.ISOUtil;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Simple Profiler
 * @author Alejandro P. Revilla
 * @author David D. Bergert
 * @version $Id$
 */
public class Profiler implements Loggeable {
    long start, partial;
    LinkedHashMap<String, Entry> events;
    public static final int TO_MILLIS = 1000000;

    public Profiler () {
        super();
        reset();
    }
    /**
     * reset timers
     */
    public void reset() {
        start = partial = System.nanoTime();
        events = new LinkedHashMap<>();
    }
    /**
     * mark checkpoint
     * @param detail checkpoint information
     */
    @SuppressWarnings("unchecked")
    public synchronized void checkPoint (String detail) {
        long now = System.nanoTime();
        Entry e = new Entry();
        e.setDurationInNanos(now - partial);
        e.setTotalDurationInNanos(now - start);
        if (events.containsKey(detail)) {
            for (int i=1; ;i++) {
                String d = detail + "-" + i;
                if (!events.containsKey (d)) {
                    detail = d;
                    break;
                }
            }
        }
        e.setEventName(detail);
        events.put (detail, e);
        partial = now;
    }
    /**
     * @return total elapsed time since last reset
     */
    public long getElapsed() {
        return System.nanoTime() - start;
    }
    public long getElapsedInMillis() {
        return getElapsed() / TO_MILLIS;
    }
    /**
     * @return parcial elapsed time since last reset
     */
    public long getPartial() {
        return System.nanoTime() - partial;
    }
    public long getPartialInMillis() {
        return getPartial() / TO_MILLIS;
    }
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        if (!events.containsKey("end"))
            checkPoint ("end");
        Collection c = events.values();
        Iterator iter = c.iterator();
        p.println (indent + "<profiler>");
        while (iter.hasNext()) 
            p.println (inner + ISOUtil.normalize(iter.next().toString()));
        p.println (indent + "</profiler>");
    }
    public LinkedHashMap<String, Entry> getEvents() {
        return events;
    }
    public Entry getEntry(String eventName) {
         return events.get(eventName);
    }
    public void reenable() {
        events.remove("end");
    }
    public static class Entry  {
        String  eventName;
        long    duration;
        long    totalDuration;          
        public Entry() {
           eventName     = "";
           duration      = 0L;
           totalDuration = 0L;        
        }
        public void setEventName (String myEvent) {
            this.eventName = myEvent;
        }
        public String getEventName () {
            return eventName;
        }    
        public void setDurationInNanos (long duration) {
            this.duration = duration;
        }
        public long getDuration () {
            return duration / TO_MILLIS;
        }
        public long getDurationInNanos() {
            return duration;
        }
        public void setTotalDurationInNanos (long totalDuration) {
            this.totalDuration = totalDuration;
        }
        public long getTotalDuration () {
            return totalDuration / TO_MILLIS;
        }
        public long getTotalDurationInNanos () {
            return totalDuration;
        }
        public String toString()  {
            StringBuilder sb = new StringBuilder (eventName);
            sb.append (" [");
            sb.append (getDuration());
            sb.append ('.');
            sb.append (duration % TO_MILLIS / 100000);
            sb.append ('/');
            sb.append (getTotalDuration ());
            sb.append ('.');
            sb.append (totalDuration % TO_MILLIS / 100000);
            sb.append (']');
            return sb.toString();
        }            
    }
}
