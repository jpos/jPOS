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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A specific log listener that filters logs based on
 * their priorities. Priorities are ordered as follows:
 * {@code TRACE < DEBUG < INFO < WARN < ERROR < FATAL}.
 * Default priority is {@code Log.INFO}.
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
public class FilterLogListener implements LogListener,Configurable
{

    private static final Map<String, Integer> LEVELS = new HashMap<>();

    static {
            LEVELS.put(Log.TRACE, 1);
            LEVELS.put(Log.DEBUG, 2);
            LEVELS.put(Log.INFO, 3);
            LEVELS.put(Log.WARN, 4);
            LEVELS.put(Log.ERROR, 5);
            LEVELS.put(Log.FATAL, 6);
    }

    private String priority = Log.INFO;
    PrintStream p;

    /** Creates a FilterLogListener that writes to {@code System.out}. */
    public FilterLogListener() {
        super();
        p = System.out;
    }

    /**
     * Creates a FilterLogListener that writes to the given PrintStream.
     * @param p the PrintStream to write log events to
     */
    public FilterLogListener(PrintStream p) {
        super();
        setPrintStream(p);
    }

    @Override
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        try {
            String log_priority = cfg.get("priority");
            if ( log_priority != null && !log_priority.trim().equals("") && LEVELS.containsKey(log_priority) ) {
                priority = log_priority;
            }
        } catch (Exception e) {
            throw new ConfigurationException (e);
        }
    }



    /**
     * Sets the PrintStream to write log events to.
     * @param p the PrintStream to write log events to
     */
    public synchronized void setPrintStream(PrintStream p) {
        this.p = p;
    }

    /** Closes the underlying PrintStream and releases it. */
    public synchronized void close() {
        if (p != null) {
            p.close();
            p = null;
        }
    }

    /**
     * Returns the current minimum log priority level.
     * @return the current priority level string
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Sets the minimum priority level for log events to be written.
     * @param priority the minimum priority level (e.g. {@code "debug"}, {@code "info"}, {@code "warn"}, {@code "error"})
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Returns true if the given log level meets or exceeds the configured priority threshold.
     * @param tagLevel the level of the event being evaluated
     * @return true if the event should be logged
     */
    public boolean permitLogging(String tagLevel) {
        Integer i = LEVELS.get(tagLevel);

        if (i == null)
            i = LEVELS.get(Log.INFO);

        Integer j = LEVELS.get(priority);

        return i >= j;
    }

    @Override
    public synchronized LogEvent log(LogEvent ev) {
        if (p != null && permitLogging(ev.getTag())) {
            Date d = new Date();
            p.println(
                    "<log realm=\"" + ev.getRealm() + "\" at=\"" + d.toString()
                    + "." + d.getTime() % 1000 + "\">"
            );
            ev.dump(p, "  ");
            p.println("</log>");
            p.flush();
        }
        return ev;
    }
}
