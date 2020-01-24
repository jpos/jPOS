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
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import java.util.List;

/**
 * Protects selected fields from LogEvents.
 *
 * ProtectedLogListener acts like a filter for Event logs,
 * it should be defined _before_ other standard LogListeners
 * such as SimpleLogListener or RotateLogListeners.<br>
 * i.e.
 * <pre>
 * <logger name="qsp">
 *   <log-listener class="org.jpos.util.SimpleLogListener"/>
 *   <log-listener class="org.jpos.util.ProtectedLogListener">
 *     <property name="protect" value="2 35 45 55" />
 *     <property name="wipe"    value="48" />
 *   </log-listener>
 *   <log-listener class="org.jpos.util.RotateLogListener">
 *     <property name="file" value="/tmp/qsp.log" />
 *     <property name="window" value="86400" />
 *     <property name="copies" value="5" />
 *     <property name="maxsize" value="1000000" />
 *   </log-listener>
 * </logger>
 * </pre>
 * 
 * Order is important. In the previous example SimpleLogListener
 * will dump unprotected LogEvents while RotateLogListener will
 * dump protected ones (for selected fields)
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.core.Configurable
 * @since jPOS 1.3.3
 */
public class ProtectedLogListener implements LogListener, Configurable
{
    String[] protectFields = null;
    String[] wipeFields    = null;
    Configuration cfg   = null;
    public static final String WIPED = "[WIPED]";
    public static final byte[] BINARY_WIPED = ISOUtil.hex2byte ("AA55AA55");

    public ProtectedLogListener () {
        super();
    }

   /**
    * Configure this ProtectedLogListener<br>
    * Properties:<br>
    * <ul>
    *  <li>[protect]   blank separated list of fields to be protected
    *  <li>[wipe]      blank separated list of fields to be wiped
    * </ul>
    * @param cfg Configuration 
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        this.cfg = cfg;
        protectFields = ISOUtil.toStringArray (cfg.get ("protect", ""));
        wipeFields    = ISOUtil.toStringArray (cfg.get ("wipe", ""));
    }
    public LogEvent log (LogEvent ev) {
        synchronized (ev.getPayLoad()) {
            final List<Object> payLoad = ev.getPayLoad();
            int size = payLoad.size();
            for (int i=0; i<size; i++) {
                Object obj = payLoad.get (i);
                if (obj instanceof ISOMsg) {
                    ISOMsg m = (ISOMsg) ((ISOMsg) obj).clone();
                    try {
                        checkProtected (m);
                        checkHidden (m);
                    } catch (ISOException e) {
                        ev.addMessage (e);
                    }
                    payLoad.set (i, m);
                } else if (obj instanceof SimpleMsg){
                    try {
                        checkProtected((SimpleMsg) obj);
                    } catch (ISOException e) {
                        ev.addMessage (e);
                    }
               }
            }
        }
        return ev;
    }
    private void checkProtected (ISOMsg m) throws ISOException {
        for (String f : protectFields) {
            Object v = null;
            try {
                v = m.getValue(f);
            } catch (ISOException ignored) {
                // NOPMD: nothing to do
            }
            if (v != null) {
                if (v instanceof String)
                    m.set(f, ISOUtil.protect((String) v));
                else
                    m.set(f, BINARY_WIPED);
            }
        }
    }
    private void checkProtected(SimpleMsg sm) throws ISOException {
        if (sm.msgContent instanceof SimpleMsg[])
            for (SimpleMsg sMsg : (SimpleMsg[]) sm.msgContent)
                checkProtected(sMsg);
        else if (sm.msgContent instanceof SimpleMsg)
            checkProtected((SimpleMsg) sm.msgContent);
        else if (sm.msgContent instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) ((ISOMsg) sm.msgContent).clone();
            checkProtected(m);
            checkHidden(m);
            sm.msgContent = m;
        }
    }
    private void checkHidden (ISOMsg m) throws ISOException {
        for (String f : wipeFields) {
            Object v = null;
            try {
                v = m.getValue(f);
            } catch (ISOException ignored) {
                // NOPMD: nothing to do
            }
            if (v != null) {
                if (v instanceof String)
                    m.set(f, WIPED);
                else
                    m.set(f, BINARY_WIPED);
            }
        }
    }
}
