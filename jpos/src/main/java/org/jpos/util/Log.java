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

/**
 * Represents a LogSource and adds several helpers
 *
 * @author apr@cs.com.uy
 * @version $Revision$ $Date$
 * @see LogSource
 */
public class Log implements LogSource {
    /** The logger used by this instance. */
    protected Logger logger;
    /** The logging realm for this instance. */
    protected String realm;

    /** Log level constant for trace messages. */
    public static final String TRACE   = "trace";
    /** Log level constant for debug messages. */
    public static final String DEBUG   = "debug";
    /** Log level constant for informational messages. */
    public static final String INFO    = "info";
    /** Log level constant for warning messages. */
    public static final String WARN    = "warn";
    /** Log level constant for error messages. */
    public static final String ERROR   = "error";
    /** Log level constant for fatal messages. */
    public static final String FATAL   = "fatal";

    /** Default constructor. */
    public Log () {
        super();
    }
    /**
     * Returns a Log instance for the given logger name and realm.
     * @param logName the logger name
     * @param realm the logging realm
     * @return a new Log instance
     */
    public static Log getLog (String logName, String realm) {
        return new Log (Logger.getLogger (logName), realm);
    }
    /**
     * Constructs a Log with the given logger and realm.
     * @param logger the logger to use
     * @param realm the logging realm
     */
    public Log (Logger logger, String realm) {
        setLogger (logger, realm);
    }
    /**
     * Sets the logger and realm for this log source.
     * @param logger the logger to use
     * @param realm the logging realm
     */
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    /**
     * Returns the logging realm.
     * @return the realm string
     */
    public String getRealm () {
        return realm;
    }
    /**
     * Returns the logger.
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }
    /**
     * Sets the logger.
     * @param logger the logger to use
     */
    public void setLogger (Logger logger) {
        this.logger = logger;
    }
    /**
     * Sets the logging realm.
     * @param realm the realm string
     */
    public void setRealm (String realm) {
        this.realm = realm;
    }
    /**
     * Logs a trace-level message.
     * @param detail the message detail
     */
    public void trace (Object detail) {
        Logger.log (createTrace (detail));
    }
    /**
     * Logs a trace-level message with an attached object.
     * @param detail the message detail
     * @param obj the object to attach
     */
    public void trace (Object detail, Object obj) {
        LogEvent evt = createTrace (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs a debug-level message.
     * @param detail the message detail
     */
    public void debug (Object detail) {
        Logger.log (createDebug (detail));
    }
    /**
     * Logs a debug-level message with an attached object.
     * @param detail the message detail
     * @param obj the object to attach
     */
    public void debug (Object detail, Object obj) {
        LogEvent evt = createDebug (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs an informational message.
     * @param detail the message detail
     */
    public void info (Object detail) {
        Logger.log (createInfo (detail));
    }
    /**
     * Logs an informational message with an attached object.
     * @param detail the message detail
     * @param obj the object to attach
     */
    public void info (Object detail, Object obj) {
        LogEvent evt = createInfo (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs a warning message.
     * @param detail the message detail
     */
    public void warn (Object detail) {
        Logger.log (createWarn (detail));
    }
    /**
     * Logs a warning message with an attached object.
     * @param detail the message detail
     * @param obj the object to attach
     */
    public void warn (Object detail, Object obj) {
        LogEvent evt = createWarn (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs an error message.
     * @param detail the message detail
     */
    public void error (Object detail) {
        Logger.log (createError (detail));
    }
    /**
     * Logs an error message with an attached object.
     * @param detail the message detail
     * @param obj the object to attach
     */
    public void error (Object detail, Object obj) {
        LogEvent evt = createError (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs a fatal message.
     * @param detail the message detail
     */
    public void fatal (Object detail) {
        Logger.log (createFatal (detail));
    }
    /**
     * Logs a fatal message with an attached object.
     * @param detail the message detail
     * @param obj the object to attach
     */
    public void fatal (Object detail, Object obj) {
        LogEvent evt = createFatal (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Creates a log event at the given level with no detail.
     * @param level the log level string
     * @return a new LogEvent
     */
    public LogEvent createLogEvent (String level) {
        return new LogEvent (this, level);
    }
    /**
     * Creates a log event at the given level with detail.
     * @param level the log level string
     * @param detail the message detail
     * @return a new LogEvent
     */
    public LogEvent createLogEvent (String level, Object detail) {
        return new LogEvent (this, level, detail);
    }
    /**
     * Creates a trace-level log event with no detail.
     * @return a new trace LogEvent
     */
    public LogEvent createTrace () {
        return createLogEvent (TRACE);
    }
    /**
     * Creates a trace-level log event with detail.
     * @param detail the message detail
     * @return a new trace LogEvent
     */
    public LogEvent createTrace (Object detail) {
        return createLogEvent (TRACE, detail);
    }
    /**
     * Creates a debug-level log event with no detail.
     * @return a new debug LogEvent
     */
    public LogEvent createDebug() {
        return createLogEvent (DEBUG);
    }
    /**
     * Creates a debug-level log event with detail.
     * @param detail the message detail
     * @return a new debug LogEvent
     */
    public LogEvent createDebug(Object detail) {
        return createLogEvent (DEBUG, detail);
    }
    /**
     * Creates an info-level log event with no detail.
     * @return a new info LogEvent
     */
    public LogEvent createInfo () {
        return createLogEvent (INFO);
    }
    /**
     * Creates an info-level log event with detail.
     * @param detail the message detail
     * @return a new info LogEvent
     */
    public LogEvent createInfo (Object detail) {
        return createLogEvent (INFO, detail);
    }
    /**
     * Creates a warn-level log event with no detail.
     * @return a new warn LogEvent
     */
    public LogEvent createWarn () {
        return createLogEvent (WARN);
    }
    /**
     * Creates a warn-level log event with detail.
     * @param detail the message detail
     * @return a new warn LogEvent
     */
    public LogEvent createWarn (Object detail) {
        return createLogEvent (WARN, detail);
    }
    /**
     * Creates an error-level log event with no detail.
     * @return a new error LogEvent
     */
    public LogEvent createError () {
        return createLogEvent (ERROR);
    }
    /**
     * Creates an error-level log event with detail.
     * @param detail the message detail
     * @return a new error LogEvent
     */
    public LogEvent createError (Object detail) {
        return createLogEvent (ERROR, detail);
    }
    /**
     * Creates a fatal-level log event with no detail.
     * @return a new fatal LogEvent
     */
    public LogEvent createFatal () {
        return createLogEvent (FATAL);
    }
    /**
     * Creates a fatal-level log event with detail.
     * @param detail the message detail
     * @return a new fatal LogEvent
     */
    public LogEvent createFatal (Object detail) {
        return createLogEvent (FATAL, detail);
    }
}
