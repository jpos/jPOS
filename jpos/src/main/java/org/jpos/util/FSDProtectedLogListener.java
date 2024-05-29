/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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
import org.jpos.iso.FSDISOMsg;
import org.jpos.iso.ISOUtil;
import java.util.List;

/**
 * Protects selected fields from LogEvents.
 *
 * FSDProtectedLogListener acts like a filter for Event logs,
 * it should be defined _before_ other standard LogListeners
 * such as SimpleLogListener or RotateLogListeners. It is
 * based upon the ProtectedLogListener<br>
 * i.e.
 * <pre>{@code
 * <logger name="Q2">
 *   <log-listener class="org.jpos.util.SimpleLogListener"/>
 *   <log-listener class="org.jpos.util.FSDProtectedLogListener">
 *     <property name="truncate" value="field1:100 field2:50" />
 *     <property name="protect" value="account-number track2-data" />
 *     <property name="wipe"    value="pinblock" />
 *   </log-listener>
 *   <log-listener class="org.jpos.util.RotateLogListener">
 *     <property name="file" value="/log/q2.log" />
 *     <property name="window" value="86400" />
 *     <property name="copies" value="5" />
 *     <property name="maxsize" value="1000000" />
 *   </log-listener>
 * </logger>
 * }</pre>
 * 
 * Order is important. In the previous example SimpleLogListener
 * will dump unprotected LogEvents while RotateLogListener will
 * dump protected ones (for selected fields)
 *
 * @author Alejandro P. Revilla
 * @author Dave Bergert 
 * @version $Revision$ 
 * @see org.jpos.core.Configurable
 * @since jPOS 1.6.5
 */

@SuppressWarnings ("unused")
public class FSDProtectedLogListener implements LogListener, Configurable
{
    String[] protectFields = null;
    String[] wipeFields    = null;
    String[] truncateFields    = null;
    boolean truncateAddEllipsis = false;
    Configuration cfg   = null;
    public static final String WIPED = "[WIPED]";
    public static final byte[] BINARY_WIPED = ISOUtil.hex2byte ("AA55AA55");

    public FSDProtectedLogListener () {
        super();
    }

   /**
    * Configure this FSDProtectedLogListener<br>
    * Properties:<br>
    * <ul>
    *  <li>[protect]   blank separated list of fields to be protected
    *  <li>[wipe]      blank separated list of fields to be wiped
    *  <li>[truncate]  blank separated list of fields:length to be truncated
    * </ul>
    * @param cfg Configuration 
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        this.cfg = cfg;
        truncateFields  = ISOUtil.toStringArray (cfg.get ("truncate", ""));
        protectFields   = ISOUtil.toStringArray (cfg.get ("protect", ""));
        wipeFields      = ISOUtil.toStringArray (cfg.get ("wipe", ""));
        truncateAddEllipsis = cfg.getBoolean("truncate-add-ellipsis", false);
    }
    public synchronized LogEvent log (LogEvent ev) {
        synchronized (ev.getPayLoad()) {
            final List<Object> payLoad = ev.getPayLoad();
            int size = payLoad.size();
            for (int i=0; i<size; i++) {
                Object obj = payLoad.get (i);
                if (obj instanceof FSDISOMsg) {
                    FSDISOMsg m = (FSDISOMsg) ((FSDISOMsg) obj).clone();
                    checkTruncated (m);
                    checkProtected (m);
                    checkHidden (m);
                    payLoad.set (i, m);
                } else if (obj instanceof FSDMsg) {
                    FSDMsg m = (FSDMsg) ((FSDMsg) obj).clone();
                    checkTruncated (m);
                    checkProtected (m);
                    checkHidden (m);
                    payLoad.set (i, m);
                }
            }
        }
        return ev;
    }

    private void checkHidden(FSDISOMsg m) {
        checkHidden(m.getFSDMsg());
    }

    private void checkProtected(FSDISOMsg m) {
        checkProtected(m.getFSDMsg());
    }

    private void checkTruncated(FSDISOMsg m) {
        checkTruncated(m.getFSDMsg());
    }

    private void checkTruncated(FSDMsg m) {
        for (String truncateField : truncateFields) {
            String truncate[] = truncateField.split(":");
            if (truncate.length == 2) {
                String f = truncate[0];
                int len = Integer.parseInt(truncate[1]);
                String v = null;
                try {
                    v = m.get(f);
                } catch (Exception ignored) {
                    // NOPMD: NOP
                }
                if (v != null && v.length() > len) {
                    m.set(f, v.substring(0, len) + (truncateAddEllipsis ? "...":""));
                }
            }
        }

    }
    private void checkProtected (FSDMsg m) {
        for (String f : protectFields) {
            String v = null;
            try {
                v = m.get(f);
            } catch (Exception ignored) {
                // NOPMD: ignore error
            }
            if (v != null) {
                m.set (f, ISOUtil.protect(v));
            }
        }
    }
    private void checkHidden (FSDMsg m) {
        for (String f : wipeFields) {
            Object v = null;
            try {
                v = m.get (f);
            } catch (Exception ignored) {
                // NOPMD ignore error
            }
            if (v != null) {
                m.set (f, WIPED);
            }
        }
    }
}
                                                                            
