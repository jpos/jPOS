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

package org.jpos.core;

import org.jpos.iso.ISOException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see Configurable
 * @since jPOS 1.2
 */
public class ConfigurationException extends ISOException {

    private static final long serialVersionUID = -5605240786314946532L;
    public ConfigurationException () {
        super();
    }
    public ConfigurationException (String detail) {
        super (detail);
    }
    public ConfigurationException (Throwable nested) {
        super (nested);
    }
    public ConfigurationException (String detail, Throwable nested) {
        super (detail, nested);
    }
}
