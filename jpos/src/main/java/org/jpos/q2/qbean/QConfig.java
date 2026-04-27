/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

/**
 * QBean that publishes its {@link Configuration} into {@link NameRegistrar}
 * so other beans can resolve it by name.
 */
@SuppressWarnings("unused")
public class QConfig extends QBeanSupport {
    /** Default constructor; no instance state to initialise. */
    public QConfig() {}
    /** Prefix used when registering the configuration in {@link NameRegistrar}. */
    public static final String PREFIX = "config.";

    @Override
    protected void initService() {
        NameRegistrar.register(PREFIX + getName(), cfg);
    }

    @Override
    protected void destroyService() {
        NameRegistrar.unregister (PREFIX + getName());
    }
    /**
     * Looks up the {@link Configuration} registered under {@code name}.
     *
     * @param name configuration name (without the {@link #PREFIX} prefix)
     * @return the registered configuration
     * @throws NameRegistrar.NotFoundException if no configuration is registered under that name
     */
    public static Configuration getConfiguration (String name)
            throws NameRegistrar.NotFoundException
    {
        return (Configuration) NameRegistrar.get(PREFIX + name);
    }

    /**
     * Looks up the {@link Configuration} registered under {@code name}, waiting up to {@code timeout}.
     *
     * @param name configuration name
     * @param timeout in millis
     * @return Configuration object or null
     */
    public static Configuration getConfiguration (String name, long timeout) {
        return (Configuration) NameRegistrar.get(PREFIX + name, timeout);
    }
}
