/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
    
    /** Default constructor. */
    public IFB_AMOUNT() {
        super();
        interpreter = BCDInterpreter.LEFT_PADDED;
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     * @param pad if true, apply padding
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
     * @exception ISOException on ISO processing error
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
     * @exception ISOException on ISO processing error
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        if (getLength() < 1)
            throw new ISOException("Amount length is not configured");
        int packedLength = getMaxPackedLength();
        checkAvailable(b, offset, packedLength, "amount");
        checkBCD(b, offset + 1, getLength() - 1);
        String d = new String(b, offset, 1)
                    + interpreter.uninterpret(b, offset + 1, getLength() - 1);
        c.setValue(d);
        return packedLength;
    }

    private void checkBCD(byte[] b, int offset, int digits) throws ISOException {
        int packedLength = interpreter.getPackedLength(digits);
        for (int i = 0; i < packedLength; i++) {
            int value = b[offset + i] & 0xFF;
            int high = value >>> 4;
            int low = value & 0x0F;
            if (high > 9 || low > 9)
                throw new ISOException("Invalid BCD digit in amount");
        }
        if ((digits & 1) != 0) {
            int padding = pad ? b[offset] >>> 4 : b[offset + packedLength - 1] & 0x0F;
            if (padding != 0)
                throw new ISOException("Invalid BCD padding in amount");
        }
    }
    public int getMaxPackedLength() {
        return 1 + (getLength() >> 1);
    }
}
