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
 * @author apr@cs.com.uy & dflc@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see IFA_LLNUM
 */
public class IF_NOP extends ISOFieldPackager {
    public IF_NOP () {
        super(0, "<dummy>");
    }

    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IF_NOP (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) {
        return new byte[0];
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     */
    public int unpack (ISOComponent c, byte[] b, int offset) {
        return 0;
    }
    public int getMaxPackedLength() {
        return 0;
    }
}
