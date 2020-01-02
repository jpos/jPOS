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
 * ISOFieldPackager ASCII AMOUNT.
 * This packager pads the amount to the left with zeros, prepends the sign amount, and
 * interprets the chars with an ASCII interpreter. It has no length prefix.
 *
 * @author jonathan.oconnor@xcom.de
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOFieldPackager
 */
public class IFA_AMOUNT extends ISOAmountFieldPackager {
    public IFA_AMOUNT() {
        super(0, null, LeftPadder.ZERO_PADDER, AsciiInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_AMOUNT(int len, String description) {
        super(len, description, LeftPadder.ZERO_PADDER, AsciiInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }
}
