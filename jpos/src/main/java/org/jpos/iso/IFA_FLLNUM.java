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
 * ISOFieldPackager ASCII variable len padded (fixed) NUMERIC
 * (suitable to use in ANSI X9.2 interchanges.
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see IFA_LLNUM
 */
public class IFA_FLLNUM extends ISOFilledStringFieldPackager {
    public IFA_FLLNUM() {
        super(RightPadder.SPACE_PADDER, AsciiInterpreter.INSTANCE, AsciiPrefixer.LL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_FLLNUM(int len, String description) {
        super(len, description, RightPadder.SPACE_PADDER, AsciiInterpreter.INSTANCE, AsciiPrefixer.LL);
        checkLength(len, 99);
    }

    public void setLength(int len)
    {
        checkLength(len, 99);
        super.setLength(len);
    }
}
