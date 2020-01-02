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

package org.jpos.q2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@SuppressWarnings("unused")
public class Activator implements BundleActivator {
    Q2 q2;

    @Override
    public void start(BundleContext context) throws Exception {
        if ("true".equalsIgnoreCase(context.getProperty("org.jpos.q2.autostart")))
            (q2 = new Q2(new String[] {}, context)).start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (q2 != null)
            q2.stop();
    }
}
