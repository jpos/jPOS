/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jpos.util.NameRegistrar;

import java.util.Map;

/**
 * Utility service used to query the NameRegistrar via JMX.
 *
 * @author <a href="mailto:nevyn@debian.org">Alwyn Schoeman</a>
 * @version $Revision$ $Date$
 * @jmx:mbean description="Name Registrar Inspector"
 */
public class NameRegistrarInspector implements NameRegistrarInspectorMBean {

    /**
     * @jmx:managed-constructor description="Empty default constructor"
     */
    public NameRegistrarInspector () {
    }

    /**
     * @jmx:managed-attribute description="Registry contents"
     */
    public Map getRegistry () {
        return NameRegistrar.getMap ();
    }
}

