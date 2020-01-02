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

/**
 * LogSources can choose to extends this SimpleLogSource
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see LogSource
 */
public class SimpleLogSource implements LogSource {
    protected Logger logger;
    protected String realm;

    public SimpleLogSource () {
        super();
        logger = null;
        realm  = null;
    }
    public SimpleLogSource (Logger logger, String realm) {
        setLogger (logger, realm);
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
    public void setRealm (String realm) {
        this.realm = realm;
    }
    public void info (String detail) {
        Logger.log (new LogEvent (this, "info", detail));
    }
    public void info (String detail, Object obj) {
        LogEvent evt = new LogEvent (this, "info", detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public void warning (String detail) {
        Logger.log (new LogEvent (this, "warning", detail));
    }
    public void warning (String detail, Object obj) {
        LogEvent evt = new LogEvent (this, "warning", detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public void error (String detail) {
        Logger.log (new LogEvent (this, "error", detail));
    }
    public void error (String detail, Object obj) {
        LogEvent evt = new LogEvent (this, "error", detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
}

