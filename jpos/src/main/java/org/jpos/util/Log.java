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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a LogSource and adds several helpers
 *
 * @author apr@cs.com.uy
 * @version $Revision$ $Date$
 * @see LogSource
 */
public class Log implements LogSource {
    /** Logger receiving the events produced by this {@code Log}. */
    protected Logger logger;
    /** Realm associated with events emitted through this {@code Log}. */
    protected String realm;
    /** Default tags applied to every {@link LogEvent} created by this {@code Log}. */
    protected final Map<String,String> defaultTags = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Level constant for trace events. */
    public static final String TRACE   = "trace";
    /** Level constant for debug events. */
    public static final String DEBUG   = "debug";
    /** Level constant for informational events. */
    public static final String INFO    = "info";
    /** Level constant for warning events. */
    public static final String WARN    = "warn";
    /** Level constant for error events. */
    public static final String ERROR   = "error";
    /** Level constant for fatal events. */
    public static final String FATAL   = "fatal";

    /** Default constructor. */
    public Log () {
        super();
    }
    /**
     * Convenience factory returning a {@code Log} bound to the given logger name and realm.
     *
     * @param logName name of the {@link Logger} to use
     * @param realm realm to associate with emitted events
     * @return a new {@code Log} instance
     */
    public static Log getLog (String logName, String realm) {
        return new Log (Logger.getLogger (logName), realm);
    }
    /**
     * Constructs a {@code Log} bound to the given logger and realm.
     *
     * @param logger underlying {@link Logger}
     * @param realm realm to associate with emitted events
     */
    public Log (Logger logger, String realm) {
        setLogger (logger, realm);
    }
    /**
     * Replaces the underlying logger and realm.
     *
     * @param logger underlying {@link Logger}
     * @param realm realm to associate with emitted events
     */
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    /**
     * Returns the realm associated with events emitted through this {@code Log}.
     *
     * @return the configured realm
     */
    public String getRealm () {
        return realm;
    }
    /**
     * Returns the underlying logger.
     *
     * @return the {@link Logger} receiving events from this {@code Log}
     */
    public Logger getLogger() {
        return logger;
    }
    /**
     * Replaces the underlying logger, leaving the realm unchanged.
     *
     * @param logger new {@link Logger}
     */
    public void setLogger (Logger logger) {
        this.logger = logger;
    }
    /**
     * Replaces the realm associated with emitted events.
     *
     * @param realm new realm
     */
    public void setRealm (String realm) {
        this.realm = realm;
    }
    /**
     * Sets a default tag on every event produced by this {@code Log};
     * a {@code null} value removes the entry.
     *
     * @param key tag name (ignored if {@code null})
     * @param value tag value, or {@code null} to remove the tag
     */
    public void setDefaultTag(String key, String value) {
        if (key == null)
            return;
        if (value == null)
            defaultTags.remove(key);
        else
            defaultTags.put(key, value);
    }
    /**
     * Removes a previously-registered default tag.
     *
     * @param key tag name (ignored if {@code null})
     */
    public void removeDefaultTag(String key) {
        if (key != null)
            defaultTags.remove(key);
    }
    /**
     * Replaces the entire set of default tags.
     *
     * @param tags new tag set, or {@code null}/empty to clear all tags
     */
    public void setDefaultTags(Map<String,String> tags) {
        defaultTags.clear();
        if (tags != null && !tags.isEmpty())
            defaultTags.putAll(tags);
    }
    /**
     * Returns an unmodifiable snapshot of the current default tags.
     *
     * @return a snapshot of the registered default tags
     */
    public Map<String,String> getDefaultTags() {
        synchronized (defaultTags) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(defaultTags));
        }
    }
    /**
     * Decorates {@code evt} with this {@code Log}'s default tags, if any.
     *
     * @param evt the event to decorate
     * @return the same event (for chaining)
     */
    protected LogEvent applyDefaultTags(LogEvent evt) {
        synchronized (defaultTags) {
            if (!defaultTags.isEmpty())
                evt.withTags(defaultTags);
        }
        return evt;
    }
    /**
     * Logs a trace event with the given detail.
     *
     * @param detail event payload
     */
    public void trace (Object detail) {
        Logger.log (createTrace (detail));
    }
    /**
     * Logs a trace event with the given detail and an additional payload.
     *
     * @param detail event payload
     * @param obj additional message appended to the event
     */
    public void trace (Object detail, Object obj) {
        LogEvent evt = createTrace (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs a debug event with the given detail.
     *
     * @param detail event payload
     */
    public void debug (Object detail) {
        Logger.log (createDebug (detail));
    }
    /**
     * Logs a debug event with the given detail and an additional payload.
     *
     * @param detail event payload
     * @param obj additional message appended to the event
     */
    public void debug (Object detail, Object obj) {
        LogEvent evt = createDebug (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs an info event with the given detail.
     *
     * @param detail event payload
     */
    public void info (Object detail) {
        Logger.log (createInfo (detail));
    }
    /**
     * Logs an info event with the given detail and an additional payload.
     *
     * @param detail event payload
     * @param obj additional message appended to the event
     */
    public void info (Object detail, Object obj) {
        LogEvent evt = createInfo (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs a warning event with the given detail.
     *
     * @param detail event payload
     */
    public void warn (Object detail) {
        Logger.log (createWarn (detail));
    }
    /**
     * Logs a warning event with the given detail and an additional payload.
     *
     * @param detail event payload
     * @param obj additional message appended to the event
     */
    public void warn (Object detail, Object obj) {
        LogEvent evt = createWarn (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs an error event with the given detail.
     *
     * @param detail event payload
     */
    public void error (Object detail) {
        Logger.log (createError (detail));
    }
    /**
     * Logs an error event with the given detail and an additional payload.
     *
     * @param detail event payload
     * @param obj additional message appended to the event
     */
    public void error (Object detail, Object obj) {
        LogEvent evt = createError (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs a fatal event with the given detail.
     *
     * @param detail event payload
     */
    public void fatal (Object detail) {
        Logger.log (createFatal (detail));
    }
    /**
     * Logs a fatal event with the given detail and an additional payload.
     *
     * @param detail event payload
     * @param obj additional message appended to the event
     */
    public void fatal (Object detail, Object obj) {
        LogEvent evt = createFatal (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Creates a new {@link LogEvent} at the given level decorated with this {@code Log}'s default tags.
     *
     * @param level event level (one of the {@code TRACE}/{@code DEBUG}/{@code INFO}/{@code WARN}/{@code ERROR}/{@code FATAL} constants)
     * @return a new event ready to be populated and logged
     */
    public LogEvent createLogEvent (String level) {
        return applyDefaultTags(new LogEvent (this, level));
    }
    /**
     * Creates a new {@link LogEvent} at the given level with an initial payload.
     *
     * @param level event level
     * @param detail initial event payload
     * @return a new event ready to be populated and logged
     */
    public LogEvent createLogEvent (String level, Object detail) {
        return applyDefaultTags(new LogEvent (this, level, detail));
    }
    /**
     * Creates an empty trace-level event.
     *
     * @return a new {@link LogEvent} at the {@link #TRACE} level
     */
    public LogEvent createTrace () {
        return createLogEvent (TRACE);
    }
    /**
     * Creates a trace-level event with the given payload.
     *
     * @param detail initial event payload
     * @return a new {@link LogEvent} at the {@link #TRACE} level
     */
    public LogEvent createTrace (Object detail) {
        return createLogEvent (TRACE, detail);
    }
    /**
     * Creates an empty debug-level event.
     *
     * @return a new {@link LogEvent} at the {@link #DEBUG} level
     */
    public LogEvent createDebug() {
        return createLogEvent (DEBUG);
    }
    /**
     * Creates a debug-level event with the given payload.
     *
     * @param detail initial event payload
     * @return a new {@link LogEvent} at the {@link #DEBUG} level
     */
    public LogEvent createDebug(Object detail) {
        return createLogEvent (DEBUG, detail);
    }
    /**
     * Creates an empty info-level event.
     *
     * @return a new {@link LogEvent} at the {@link #INFO} level
     */
    public LogEvent createInfo () {
        return createLogEvent (INFO);
    }
    /**
     * Creates an info-level event with the given payload.
     *
     * @param detail initial event payload
     * @return a new {@link LogEvent} at the {@link #INFO} level
     */
    public LogEvent createInfo (Object detail) {
        return createLogEvent (INFO, detail);
    }
    /**
     * Creates an empty warning-level event.
     *
     * @return a new {@link LogEvent} at the {@link #WARN} level
     */
    public LogEvent createWarn () {
        return createLogEvent (WARN);
    }
    /**
     * Creates a warning-level event with the given payload.
     *
     * @param detail initial event payload
     * @return a new {@link LogEvent} at the {@link #WARN} level
     */
    public LogEvent createWarn (Object detail) {
        return createLogEvent (WARN, detail);
    }
    /**
     * Creates an empty error-level event.
     *
     * @return a new {@link LogEvent} at the {@link #ERROR} level
     */
    public LogEvent createError () {
        return createLogEvent (ERROR);
    }
    /**
     * Creates an error-level event with the given payload.
     *
     * @param detail initial event payload
     * @return a new {@link LogEvent} at the {@link #ERROR} level
     */
    public LogEvent createError (Object detail) {
        return createLogEvent (ERROR, detail);
    }
    /**
     * Creates an empty fatal-level event.
     *
     * @return a new {@link LogEvent} at the {@link #FATAL} level
     */
    public LogEvent createFatal () {
        return createLogEvent (FATAL);
    }
    /**
     * Creates a fatal-level event with the given payload.
     *
     * @param detail initial event payload
     * @return a new {@link LogEvent} at the {@link #FATAL} level
     */
    public LogEvent createFatal (Object detail) {
        return createLogEvent (FATAL, detail);
    }
}
