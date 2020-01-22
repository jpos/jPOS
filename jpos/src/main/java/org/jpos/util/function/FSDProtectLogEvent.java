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
package org.jpos.util.function;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.FSDISOMsg;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;

import java.util.List;

/**
 * FSDProtectedLogEvent protects selected fields in a LogEvent when writing the
 * event to output.
 *
 * When used as an event mapper with {@link org.jpos.util.MappingLogEventWriter} it
 * is configured like following <br>
 *     <pre>
 *         <writer class="org.jpos.util.MappingLogEventWriter">
 *             <event-mapper class="org.jpos.util.function.FSDProtectLogEvent">
 *                 <property name="protect" value="2 35 45 55"/>
 *                 <property name="wipe" value="48"/>
 *             </event-mapper>
 *         </writer>
 *     </pre>
 * Note that MappingLogEventWriter can have multiple event mappers defined and they are
 * applied in the order of specification.
 * @author Alejandro P. Revilla
 * @author Dave Bergert
 * @author Alwyn Schoeman
 * @since 2.1.4
 */
public class FSDProtectLogEvent implements LogEventMapper, Configurable {
    String[] protectFields = null;
    String[] wipeFields    = null;
    String[] truncateFields    = null;
    Configuration cfg   = null;
    public static final String WIPED = "[WIPED]";

    @Override
    public LogEvent apply(LogEvent logEvent) {
        synchronized (logEvent.getPayLoad()) {
            final List<Object> payLoad = logEvent.getPayLoad();
            int size = payLoad.size();
            for (int i=0; i<size; i++) {
                Object obj = payLoad.get (i);
                if (obj instanceof FSDISOMsg) {
                    FSDISOMsg m = (FSDISOMsg) ((FSDISOMsg) obj).clone();
                    try {
                        checkTruncated (m);
                        checkProtected (m);
                        checkHidden (m);
                    } catch (ISOException e) {
                        logEvent.addMessage (e);
                    }
                    payLoad.set (i, m);
                }
            }
        }
        return logEvent;
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        truncateFields  = ISOUtil.toStringArray (cfg.get ("truncate", ""));
        protectFields   = ISOUtil.toStringArray (cfg.get ("protect", ""));
        wipeFields      = ISOUtil.toStringArray (cfg.get ("wipe", ""));
    }

    private void checkTruncated (FSDISOMsg m) throws ISOException {
         for (String truncateField : truncateFields) {
             String[] truncate = truncateField.split(":");
             if (truncate.length == 2) {
                 String f = truncate[0];
                 int len = Integer.parseInt(truncate[1]);
                 String v = null;
                 try {
                     v = m.getFSDMsg().get(f);
                 } catch (Exception ignored) {
                     // NOPMD: NOP
                 }
                 if (v != null) {
                     if (v.length() > len) {
                         m.getFSDMsg().set(f, v.substring(0, len));
                     }
                 }
             }
         }
    }
    private void checkProtected (FSDISOMsg m) throws ISOException {
        for (String f : protectFields) {
            String v = null;
            try {
                v = m.getFSDMsg().get(f);
            } catch (Exception ignored) {
                // NOPMD: ignore error
            }
            if (v != null) {
                m.getFSDMsg().set (f, ISOUtil.protect(v));
            }
        }
    }
    private void checkHidden (FSDISOMsg m) throws ISOException {
        for (String f : wipeFields) {
            String v = null;
            try {
                v = m.getFSDMsg().get (f);
            } catch (Exception ignored) {
                // NOPMD ignore error
            }
            if (v != null) {
                m.getFSDMsg().set (f, WIPED);
            }
        }
    }

}
