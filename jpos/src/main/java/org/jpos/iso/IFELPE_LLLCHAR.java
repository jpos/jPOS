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

    /** Default constructor. */
    public IFELPE_LLLCHAR() {
        super();
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFELPE_LLLCHAR(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException on ISO processing error
     */
    @Override
    public byte[] pack(final ISOComponent c) throws ISOException {
        final String s = (String) c.getValue();
        final int len = s.length();
        if (len > getLength() || len > 997)
            throw new ISOException(
                    "invalid len " + len + " packing IFELPE_LLLCHAR field "
                            + c.getKey() + " maxlen=" + getLength()
            );
        final byte[] payload = new byte[len + TAG_HEADER_LENGTH];
        final String tagHeader = ISOUtil.zeropad(Integer.toString(len + TAG_BYTE_LENGTH), LENGTH_BYTE_LENGTH)
                + ISOUtil.zeropad(c.getKey().toString(), TAG_BYTE_LENGTH);
        System.arraycopy(ISOUtil.asciiToEbcdic(tagHeader), 0, payload, 0, TAG_HEADER_LENGTH);
        System.arraycopy(ISOUtil.asciiToEbcdic(s), 0, payload, TAG_HEADER_LENGTH, len);
        return payload;
    }

    @Override
    public int unpack(final ISOComponent c, final byte[] b, final int offset) throws ISOException {
        if (!(c instanceof ISOField)) throw new ISOException(c.getClass()
                .getName()
                + " is not an ISOField");

        checkAvailable(b, offset, TAG_HEADER_LENGTH, "field header");
        final int inclusiveLength = EbcdicPrefixer.LLL.decodeLength(b, offset);
        final int fieldNumber = EbcdicPrefixer.LL.decodeLength(b, offset + LENGTH_BYTE_LENGTH);
        if (inclusiveLength < TAG_BYTE_LENGTH)
            throw new ISOException("Invalid self-inclusive field length " + inclusiveLength);
        final int len = inclusiveLength - TAG_BYTE_LENGTH;
        checkUnpackedLength(len);
        checkAvailable(b, offset + TAG_HEADER_LENGTH, len, "field value");
        c.setFieldNumber(fieldNumber);
        c.setValue(ISOUtil.ebcdicToAscii(b, offset + TAG_HEADER_LENGTH, len));
        return len + TAG_HEADER_LENGTH;
    }

    @Override
    public void unpack(final ISOComponent c, final InputStream in) throws IOException,
            ISOException {
        if (!(c instanceof ISOField)) throw new ISOException(c.getClass()
                .getName()
                + " is not an ISOField");

        final byte[] header = readBytes(in, TAG_HEADER_LENGTH);
        final int inclusiveLength = EbcdicPrefixer.LLL.decodeLength(header, 0);
        final int fieldNumber = EbcdicPrefixer.LL.decodeLength(header, LENGTH_BYTE_LENGTH);
        if (inclusiveLength < TAG_BYTE_LENGTH)
            throw new ISOException("Invalid self-inclusive field length " + inclusiveLength);
        final int len = inclusiveLength - TAG_BYTE_LENGTH;
        checkUnpackedLength(len);
        c.setFieldNumber(fieldNumber);
        c.setValue(ISOUtil.ebcdicToAscii(readBytes(in, len)));
    }

    @Override
    public int getMaxPackedLength() {
        return getLength() + TAG_HEADER_LENGTH;
    }
}
