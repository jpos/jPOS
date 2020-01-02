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
 * ISOFieldPackager CHARACTERS (ASCII & BINARY)
 * EBCDIC version of IF_CHAR
 * @author apr@cs.com.uy
 * @version $Id$
 * @see IF_CHAR
 * @see ISOComponent
 */
public class IFE_CHAR extends ISOStringFieldPackager {
    /** Used for the GenericPackager. */
    public IFE_CHAR() {
        super(0, null, RightTPadder.SPACE_PADDER, EbcdicInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }

    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_CHAR(int len, String description) {
        super(len, description, RightTPadder.SPACE_PADDER, EbcdicInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }
}
