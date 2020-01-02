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

package org.jpos.iso;

/**
 * ISOFieldPackager ASCII NUMERIC.
 * Left padder with zeros, ASCII Interpretation, and no length prefix.
 *
 * @author apr@cs.com.uy
 * @author jonathan.oconnor@xcom.de
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_NUMERIC extends ISOStringFieldPackager {
    public IFA_NUMERIC() {
        super(LeftPadder.ZERO_PADDER, AsciiInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_NUMERIC(int len, String description) {
        super(len, description, LeftPadder.ZERO_PADDER, AsciiInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }
}
