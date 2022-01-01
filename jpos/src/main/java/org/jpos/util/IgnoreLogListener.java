/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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

import java.util.Arrays;
import java.util.List;

/*
 * Sample use:
 *
 *  <log-listener class="org.jpos.util.IgnoreLogListener">
 *   <property name="realm" value="mychannel" />
 *   <property name="mti" value="0800" />
 *   <property name="mti" value="0810" />
 *   <property name="ignore" value="50" />
 * </log-listener>
 */

public class IgnoreLogListener implements LogListener, Configurable {
    String[] realms;
    String[] mtis;
    int ignore;
    int ignored = 0;

    public IgnoreLogListener() {
        super();
    }

    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        mtis = cfg.getAll("mti");
        realms = cfg.getAll("realm");
        ignore = cfg.getInt("ignore", 100);
    }
    public LogEvent log (LogEvent ev) {
        if (hasRealm(ev.getRealm())) {
            synchronized (ev.getPayLoad()) {
                final List<Object> payLoad = ev.getPayLoad();
                for (Object obj : payLoad) {
                    if (obj instanceof ISOMsg) {
                        ISOMsg m = (ISOMsg) obj;
                        try {
                            if (hasMti(m.getMTI())) {
                                if (++ignored > ignore) {
                                    ev.addMessage("ignored " + ignore + " similar messages");
                                    ignored = 0;
                                    return ev;
                                } else {
                                    return null;
                                }
                            }
                        } catch (ISOException e) {
                            ev.addMessage(e);
                        }
                    }
                }
            }
        }
        return ev;
    }

    private boolean hasMti (String mti) {
        return Arrays.stream(mtis).anyMatch (m -> mti != null && mti.startsWith(m));
    }

    private boolean hasRealm(String realm) {
        return Arrays.stream(realms).anyMatch(r -> realm != null && realm.startsWith(r));
    }
}

