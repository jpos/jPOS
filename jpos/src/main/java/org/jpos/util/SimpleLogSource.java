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
 * LogSources can choose to extends this SimpleLogSource
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see LogSource
 */
public class SimpleLogSource implements LogSource {
    /** The logger used by this log source. */
    protected Logger logger;
    /** The logging realm for this log source. */
    protected String realm;

    /** Default constructor. */
    public SimpleLogSource () {
        super();
        logger = null;
        realm  = null;
    }
    /**
     * Constructs a SimpleLogSource with the given logger and realm.
     * @param logger the logger to use
     * @param realm the logging realm
     */
    public SimpleLogSource (Logger logger, String realm) {
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
     * Sets the logging realm.
     * @param realm the realm string
     */
    public void setRealm (String realm) {
        this.realm = realm;
    }
    /**
     * Logs an informational message.
     * @param detail the message text
     */
    public void info (String detail) {
        Logger.log (new LogEvent (this, "info", detail));
    }
    /**
     * Logs an informational message with an attached object.
     * @param detail the message text
     * @param obj the object to attach
     */
    public void info (String detail, Object obj) {
        LogEvent evt = new LogEvent (this, "info", detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs a warning message.
     * @param detail the warning text
     */
    public void warning (String detail) {
        Logger.log (new LogEvent (this, "warning", detail));
    }
    /**
     * Logs a warning message with an attached object.
     * @param detail the warning text
     * @param obj the object to attach
     */
    public void warning (String detail, Object obj) {
        LogEvent evt = new LogEvent (this, "warning", detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    /**
     * Logs an error message.
     * @param detail the error text
     */
    public void error (String detail) {
        Logger.log (new LogEvent (this, "error", detail));
    }
    /**
     * Logs an error message with an attached object.
     * @param detail the error text
     * @param obj the object to attach
     */
    public void error (String detail, Object obj) {
        LogEvent evt = new LogEvent (this, "error", detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
}

