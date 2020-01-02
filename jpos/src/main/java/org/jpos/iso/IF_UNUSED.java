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
 * IF_UNUSED acts as a filler for unused elements of a message packager.
 * Packing or unpacking with this packager throws an ISOException.
 * Use IF_NOP if you don't want an exception thrown.
 * 
 * @author jonathan.oconnor@xcom.de
 * @version $Id$
 * @see ISOComponent
 * @see IF_NOP
 */
public class IF_UNUSED extends ISOFieldPackager {
    /**
     */
    public IF_UNUSED () {
        super(0, "<dummy>");
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        throw new ISOException("IF_UNUSED: Packager should not pack field " + c.getKey());
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     */
    public int unpack (ISOComponent c, byte[] b, int offset) throws ISOException {
        throw new ISOException("IF_UNUSED: Packager should not unpack field " + c.getKey());
    }
    public int getMaxPackedLength() {
        return 0;
    }
}
