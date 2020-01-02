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
import org.jpos.space.Space;
import org.jpos.space.SpaceError;
import org.jpos.space.SpaceFactory;
import org.jpos.space.TSpace;

public class SpaceLogListener implements LogListener, Configurable {
    String queueName;
    Space sp;
    Space buffer;
    long timeout = 300000L;
    boolean frozen = true;
    Configuration cfg;

    @SuppressWarnings("unused")
    public SpaceLogListener() {
        super();
    }

    @SuppressWarnings("unchecked")
    public synchronized LogEvent log(LogEvent ev) {
        LogEvent e = frozen ? new FrozenLogEvent(ev) : ev;
        try {
            getSpace().out(queueName, e, timeout);
            return null;
        } catch (Throwable t) {
            ev.addMessage("Unable to log to " + queueName);
            return ev;
        }
    }

    /**
     * @param cfg Configuration object
     * @throws ConfigurationException
     */
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        queueName = cfg.get("queue", null);
        if (queueName == null)
            throw new ConfigurationException("'queue' property not configured");

        timeout = cfg.getLong("timeout", timeout);
        frozen = cfg.getBoolean("frozen", true);
    }

    @SuppressWarnings("unchecked")
    private Space getSpace() {
        if (sp == null) {
            try {
                sp = SpaceFactory.getSpace(cfg.get("space"));
                if (buffer != null) {
                    while (buffer.rdp(queueName) != null)
                        sp.out (queueName, buffer.inp(queueName));
                    buffer = null;
                }
            } catch (SpaceError e) {
                return (buffer = new TSpace());
            }
        }
        return sp;
    }
}
