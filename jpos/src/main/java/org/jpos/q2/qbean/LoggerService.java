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

package org.jpos.q2.qbean;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

@SuppressWarnings("unused")
public class LoggerService extends QBeanSupport implements Runnable {
    Logger logger;
    String queueName;
    Space sp;
    long timeout = 300000L;

    protected void startService () throws ConfigurationException {
        NameRegistrar.register(getName(), this);
        new Thread(this, getName()).start();
    }
    protected void stopService() {
        NameRegistrar.unregister(getName());
    }

    @SuppressWarnings("unchecked")
    public void run() {
        while (running()) {
            LogEvent evt = (LogEvent) sp.in (queueName, 1000L);
            if (evt != null) {
                evt.setSource(getLog());
                Logger.log(evt);
            }
        }
    }

    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        queueName = cfg.get("queue", null);
        if (queueName == null)
            throw new ConfigurationException("'queue' property not configured");
        sp = SpaceFactory.getSpace(cfg.get("space"));
        timeout = cfg.getLong("timeout", timeout);
    }
}
