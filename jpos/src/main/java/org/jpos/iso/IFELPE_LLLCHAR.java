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
import java.io.IOException;
import java.io.InputStream;

/**
 * ISOFieldPackager ASCII variable len CHAR 
 * suitable for GICC subfield 60<br>
 * <code>
 * Format LLLTT....
 * Where LLL is the 3 digit field length
 *       TT is the 2 digit field number (Tag)
 *       is the field content   
 * </code>
 */
public class IFELPE_LLLCHAR extends ISOFieldPackager {
    private static final int TAG_BYTE_LENGTH = 2;
    private static final int LENGTH_BYTE_LENGTH = 3;
    private static final int TAG_HEADER_LENGTH = TAG_BYTE_LENGTH + LENGTH_BYTE_LENGTH;

    public IFELPE_LLLCHAR() {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFELPE_LLLCHAR(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    @Override
    public byte[] pack(final ISOComponent c) throws ISOException {
        final String s = (String) c.getValue();
        final int len = s.length();
        final byte[] payload = new byte[len + TAG_HEADER_LENGTH];
        final String tagHeader = ISOUtil.zeropad(Integer.toString(len + TAG_BYTE_LENGTH), LENGTH_BYTE_LENGTH)
                + ISOUtil.zeropad(c.getKey().toString(), TAG_BYTE_LENGTH);
        System.arraycopy(ISOUtil.asciiToEbcdic(tagHeader), 0, payload, 0, TAG_HEADER_LENGTH);
        System.arraycopy(ISOUtil.asciiToEbcdic(s), 0, payload, TAG_HEADER_LENGTH, len);
        return payload;
    }

    @Override
    public int unpack(final ISOComponent c, final byte[] b, final int offset) throws ISOException {
        final String asciiResult = ISOUtil.ebcdicToAscii(b, offset, LENGTH_BYTE_LENGTH);
        final int len = Integer.parseInt(asciiResult) - TAG_BYTE_LENGTH;
        if (!(c instanceof ISOField)) throw new ISOException(c.getClass()
                .getName()
                + " is not an ISOField");
        c.setFieldNumber(Integer.parseInt(ISOUtil.ebcdicToAscii(b, offset + LENGTH_BYTE_LENGTH, TAG_BYTE_LENGTH)));
        c.setValue(ISOUtil.ebcdicToAscii(b, offset + TAG_HEADER_LENGTH, len));
        return len + 5;
    }

    @Override
    public void unpack(final ISOComponent c, final InputStream in) throws IOException,
            ISOException {
        if (!(c instanceof ISOField)) throw new ISOException(c.getClass()
                .getName()
                + " is not an ISOField");

        final int len = Integer.parseInt(ISOUtil.ebcdicToAscii(readBytes(in, LENGTH_BYTE_LENGTH))) - TAG_BYTE_LENGTH;
        final int fieldNumber = Integer.parseInt(ISOUtil.ebcdicToAscii(readBytes(in, TAG_BYTE_LENGTH)));
        c.setFieldNumber(fieldNumber);
        c.setValue(new String(readBytes(in, len)));
    }

    @Override
    public int getMaxPackedLength() {
        return getLength() + TAG_BYTE_LENGTH;
    }
}
