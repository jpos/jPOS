/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

    /** Default constructor. */
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
     * Returns the total elapsed time since the last reset.
     * @return total elapsed time since last reset
     */
    public long getElapsed() {
        return System.nanoTime() - start;
    }
    /** Returns total elapsed time in milliseconds since the last reset.
     * @return elapsed time in milliseconds
     */
    public long getElapsedInMillis() {
        return getElapsed() / TO_MILLIS;
    }
    /**
     * Returns the partial elapsed time since the last checkpoint.
     * @return partial elapsed time since last reset
     */
    public long getPartial() {
        return System.nanoTime() - partial;
    }
    /** Returns partial elapsed time in milliseconds since the last checkpoint.
     * @return partial elapsed time in milliseconds
     */
    public long getPartialInMillis() {
        return getPartial() / TO_MILLIS;
    }
    /** Dumps profiler results to the given print stream.
     * @param p the output stream
     * @param indent indent prefix
     */
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
    /** Returns all profiler events collected since the last reset.
     * @return ordered map of event name to Entry
     */
    public LinkedHashMap<String, Entry> getEvents() {
        return events;
    }
    /** Returns the profiler entry for the given event name.
     * @param eventName the event name
     * @return the corresponding Entry, or {@code null} if not found
     */
    public Entry getEntry(String eventName) {
         return events.get(eventName);
    }
    /** Removes the "end" checkpoint so profiling can continue. */
    public void reenable() {
        events.remove("end");
    }
    /** A single timed checkpoint entry recorded by the Profiler. */
    public static class Entry  {
        String  eventName;
        long    duration;
        long    totalDuration;          
        /** Default constructor — initialises all fields to empty/zero. */
        public Entry() {
           eventName     = "";
           duration      = 0L;
           totalDuration = 0L;        
        }
        /** Sets the event name for this entry.
         * @param myEvent the event label
         */
        public void setEventName (String myEvent) {
            this.eventName = myEvent;
        }
        /** Returns the event name.
         * @return event name
         */
        public String getEventName () {
            return eventName;
        }    
        /** Sets the entry duration in nanoseconds.
         * @param duration duration in nanoseconds
         */
        public void setDurationInNanos (long duration) {
            this.duration = duration;
        }
        /** Returns the entry duration in milliseconds.
         * @return duration in milliseconds
         */
        public long getDuration () {
            return duration / TO_MILLIS;
        }
        /** Returns the raw entry duration in nanoseconds.
         * @return duration in nanoseconds
         */
        public long getDurationInNanos() {
            return duration;
        }
        /** Sets the total elapsed duration in nanoseconds.
         * @param totalDuration total duration in nanoseconds
         */
        public void setTotalDurationInNanos (long totalDuration) {
            this.totalDuration = totalDuration;
        }
        /** Returns the total elapsed duration in milliseconds.
         * @return total duration in milliseconds
         */
        public long getTotalDuration () {
            return totalDuration / TO_MILLIS;
        }
        /** Returns the raw total elapsed duration in nanoseconds.
         * @return total duration in nanoseconds
         */
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
