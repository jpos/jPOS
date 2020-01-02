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
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;

@SuppressWarnings("unused")
public class QConfig extends QBeanSupport {
    public static final String PREFIX = "config.";

    @Override
    protected void initService() {
        NameRegistrar.register(PREFIX + getName(), cfg);
    }

    @Override
    protected void destroyService() {
        NameRegistrar.unregister (PREFIX + getName());
    }
    public static Configuration getConfiguration (String name)
            throws NameRegistrar.NotFoundException
    {
        return (Configuration) NameRegistrar.get(PREFIX + name);
    }

    /**
     * @param name configuration name
     * @param timeout in millis
     * @return Configuration object or null
     */
    public static Configuration getConfiguration (String name, long timeout) {
        return (Configuration) NameRegistrar.get(PREFIX + name, timeout);
    }
}
