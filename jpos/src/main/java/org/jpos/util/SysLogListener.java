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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;

/**
 * SysLog Listener
 * see www.ietf.org/rfc/rfc3164.txt
 *
 * <pre>
 * &lt;log-listener class="org.jpos.util.SysLogListener"&gt;
 *    &lt;property name="facility" value="21" /&gt;
 *    &lt;property name="severity" value="5" /&gt;
 *    &lt;property name="tags" value="audit, syslog" /&gt;
 *    &lt;property name="prefix" value="[jPOS]" /&gt;
 *
 *    &lt;property name="syslog.facility" value="21" /&gt;
 *    &lt;property name="syslog.severity" value="5" /&gt;
 *
 *    &lt;property name="audit.facility" value="21" /&gt;
 *    &lt;property name="audit.severity" value="4" /&gt;
 *  &lt;/log-listener&gt;
 * </pre>
 *
 */
@SuppressWarnings("unused")
public class SysLogListener implements LogListener, Configurable {
    private DatagramSocket socket;
    private InetAddress    host;
    private String         prefix;
    private String         tags;
    private int            port;
    private int            defaultFacility;
    private int            defaultSeverity;
    private Configuration  cfg;
    public static final int SYSLOG_PORT = 514;
    public static final int LOG_USER = 16;  // local use 0
    public static final int PRI_INFO = 6;   // informational

    public SysLogListener () {
        super();
    }
    public synchronized LogEvent log (LogEvent ev) {
        if (socket != null && ev.getTag() != null && tags.contains(ev.getTag())) {
            int facility = cfg.getInt (ev.getTag() + ".facility", defaultFacility);
            int severity = cfg.getInt (ev.getTag() + ".severity", defaultSeverity);
            int priority = facility<<3 | severity;

            StringBuilder sb = new StringBuilder();
            sb.append ('<');
            sb.append (Integer.toString(priority));
            sb.append ('>');
            if (prefix != null) {
                sb.append (prefix);
                sb.append (' ');
            }
            sb.append (ev.getRealm());
            sb.append (' ');
            sb.append (ev.getTag());
            sb.append (" - ");
            synchronized (ev.getPayLoad()) {
                Iterator iter = ev.getPayLoad().iterator();
                for (int i=0; iter.hasNext(); i++) {
                    if (i>0)
                        sb.append (' ');
                    sb.append (iter.next().toString());
                }
            }
            byte[] b = sb.toString().getBytes();
            DatagramPacket packet = new DatagramPacket
                (b, Math.min (b.length, 1024), host, port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                ev.addMessage ("--- SysLogListener error ---");
                ev.addMessage (e);
            }
        }
        return ev;
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        try {
            socket = new DatagramSocket();
            port = cfg.getInt ("port", SYSLOG_PORT);
            host = InetAddress.getByName (cfg.get ("host", "localhost"));
            defaultFacility = cfg.getInt ("facility", LOG_USER);
            defaultSeverity = cfg.getInt ("severity", PRI_INFO);
            tags     = cfg.get ("tags", "audit, syslog");
            prefix = cfg.get ("prefix", null);
        } catch (Exception e) {
            throw new ConfigurationException (e);
        }
    }
}

