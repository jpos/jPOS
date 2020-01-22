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
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleMsg;

import java.util.List;

/**
 * ProtectedLogEvent protects selected fields in a LogEvent when writing the
 * event to output.
 *
 * When used as an event mapper with {@link org.jpos.util.MappingLogEventWriter} it
 * is configured like following <br>
 *     <pre>
 *         <writer class="org.jpos.util.MappingLogEventWriter">
 *             <event-mapper class="org.jpos.util.function.ProtectLogEvent">
 *                 <property name="truncate" value="field1:100 field2:50" />
 *                 <property name="protect" value="account-number track2-data"/>
 *                 <property name="wipe" value="pinblock"/>
 *             </event-mapper>
 *         </writer>
 *     </pre>
 * Note that MappingLogEventWriter can have multiple event mappers defined and they are
 * applied in the order of specification.
 *
 * @author Alejandro P. Revilla
 * @author Alwyn Schoeman
 * @since 2.1.4
 */
public class ProtectLogEvent implements LogEventMapper, Configurable {
    String[] protectFields = null;
    String[] wipeFields    = null;
    Configuration cfg   = null;
    public static final String WIPED = "[WIPED]";
    public static final byte[] BINARY_WIPED = ISOUtil.hex2byte ("AA55AA55");

    @Override
    public LogEvent apply(LogEvent logEvent) {
        synchronized (logEvent.getPayLoad()) {
            final List<Object> payLoad = logEvent.getPayLoad();
            int size = payLoad.size();
            for (int i=0; i<size; i++) {
                Object obj = payLoad.get (i);
                if (obj instanceof ISOMsg) {
                    ISOMsg m = (ISOMsg) ((ISOMsg) obj).clone();
                    try {
                        checkProtected (m);
                        checkHidden (m);
                    } catch (ISOException e) {
                        logEvent.addMessage (e);
                    }
                    payLoad.set (i, m);
                } else if (obj instanceof SimpleMsg){
                    try {
                        checkProtected((SimpleMsg) obj);
                    } catch (ISOException e) {
                        logEvent.addMessage (e);
                    }
               }
            }
        }
        return logEvent;
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        protectFields = ISOUtil.toStringArray (cfg.get ("protect", ""));
        wipeFields    = ISOUtil.toStringArray (cfg.get ("wipe", ""));
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
        Object msgContent = sm.getMsgContent();
        if (msgContent instanceof SimpleMsg[])
            for (SimpleMsg sMsg : (SimpleMsg[]) msgContent)
                checkProtected(sMsg);
        else if (msgContent instanceof SimpleMsg)
            checkProtected((SimpleMsg) msgContent);
        else if (msgContent instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) ((ISOMsg) msgContent).clone();
            checkProtected(m);
            checkHidden(m);
            sm.setMsgContent(m);
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
