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
 * ISOFieldPackager Binary Amount
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_AMOUNT extends ISOFieldPackager {
    private BCDInterpreter interpreter;
    
    public IFB_AMOUNT() {
        super();
        interpreter = BCDInterpreter.LEFT_PADDED;
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_AMOUNT(int len, String description, boolean pad) {
        super(len, description);
        this.pad = pad;
        interpreter = pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED;
    }
    
    public void setPad(boolean pad)
    {
        this.pad = pad;
        interpreter = pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED;
    }

    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        String s = (String) c.getValue();
        String amount = ISOUtil.zeropad(s.substring(1), getLength()-1);
        byte[] b   = new byte[1 + (getLength() >> 1)];
        b[0] = (byte) s.charAt(0);
        interpreter.interpret(amount, b, 1);
        return b;
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        String d = new String(b, offset, 1)
                    + interpreter.uninterpret(b, offset + 1, getLength() - 1);
        c.setValue(d);
        return 1 + (getLength() >> 1);
    }
    public int getMaxPackedLength() {
        return 1 + (getLength() >> 1);
    }
}
