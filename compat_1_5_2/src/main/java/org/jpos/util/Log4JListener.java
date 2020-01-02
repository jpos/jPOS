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

import org.apache.log4j.Level;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.xml.DOMConfigurator;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Eoin P. FLood</a>
 * @version $Revision$ $Date$
 * @see org.jpos.util.LogListener
 * @see org.jpos.util.LogEvent
 * @since jPOS 1.3
 *
 * This class acts as a simple bridge between jPOS's logging system
 * and log4j.
 * The jPOS <code>realm</code> is used as the log4j <code>Logger</code>
 * and messages are by default logged with the DEBUG level. This can
 * be changed by calling <code>setLevel</code>
 */

@SuppressWarnings("deprecation")
public class Log4JListener implements LogListener, Configurable
{
    private Level _level;
    /** 
     * Create a new Log4JListener with DEBUG level.
     */
    public Log4JListener ()
    {
        setLevel (Level.DEBUG_INT);
    }

    public void setLevel (int level) {
        _level = Level.toLevel (level);
    }

    public void setLevel (String level) {
        _level = Level.toLevel (level);
    }

    public void close() 
    {
    }

    /**
     * Expects the following properties:
     * <ul>
     *  <li>config   - Configuration file path
     *  <li>priority - Log4J priority (debug, info, warn, error)
     *  <li>watch    - interval (in ms) to monitor XML config file for changes 
     * </ul>
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        String config = cfg.get ("config");
        long watch =  cfg.getLong ("watch");

        if (watch == 0)
            watch = FileWatchdog.DEFAULT_DELAY;

        if ( config!=null && !config.trim().equals(""))
            DOMConfigurator.configureAndWatch (config, watch);

        setLevel (cfg.get ("priority"));
    }

    public synchronized LogEvent log (LogEvent ev) {

        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger (
            ev.getRealm().replace('/', ':')
        );
        if (logger.isEnabledFor ( _level)) {
            ByteArrayOutputStream w = new ByteArrayOutputStream ();
            PrintStream p = new PrintStream (w);
            ev.dump (p, "");
            logger.log (_level, w.toString());
        }
        return ev;
    }
}

