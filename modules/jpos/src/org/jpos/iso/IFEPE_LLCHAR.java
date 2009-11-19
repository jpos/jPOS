/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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

/*
 * vim:set ts=8 sw=4:
 */

/**
 * ISOFieldPackager EBCDIC variable len CHAR suitable for MasterCard subfield 48<br>
 * <code>
 * Format TTLL....
 * Where TT is the 2 digit field number (Tag)
 *       LL is the 2 digit field length
 *       ... is the field content   
 * </code>
 * 
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @author <a href="mailto:marksalter@talktalk.net">Mark Salter</a>
 * 
 * @version $Id: IFEP_LLCHAR.java 2706 2009-03-05 11:24:43Z apr $
 * @see ISOComponent
 */
public class IFEPE_LLCHAR extends ISOFieldPackager {
    public IFEPE_LLCHAR() {
        super();
    }

    /**
     * @param len
     *            - field len
     * @param description
     *            symbolic descrption
     */
    public IFEPE_LLCHAR(int len, String description) {
        super(len, description);
    }

    /**
     * @param c
     *            - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        int len;
        String s = (String) c.getValue();

        if ((len = s.length()) > getLength() || len > 97) // paranoia settings
            throw new ISOException("invalid len " + len
                    + " packing IFEPE_LLCHAR field " + (Integer) c.getKey());

        return ISOUtil.asciiToEbcdic(ISOUtil.zeropad(((Integer) c
                .getKey()).toString(), 2)
                + ISOUtil.zeropad(Integer.toString(len ), 2) + s);
    }

    /**
     * @param c
     *            - the Component to unpack
     * @param b
     *            - binary image
     * @param offset
     *            - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {

        byte[] length = new byte[2];
        System.arraycopy(b, offset+2, length, 0, 2);
        int len = Integer.parseInt(new String(ISOUtil.ebcdicToAscii(length)));

        if (!(c instanceof ISOField))
            throw new ISOException(c.getClass().getName()
                    + " is not an ISOField");

        String content = ISOUtil.ebcdicToAscii(b, offset + 4, len);

        byte[] fieldId = new byte[2];
        System.arraycopy(b, offset, fieldId, 0, 2);
        int fieldNumber = Integer.parseInt(new String(ISOUtil
                .ebcdicToAscii(fieldId)));

        ((ISOField) c).setFieldNumber(fieldNumber);

        c.setValue(content);

        return len + 2 + 2;
    }

    public void unpack(ISOComponent c, InputStream in) throws IOException,
            ISOException {

        if (!(c instanceof ISOField))
            throw new ISOException(c.getClass().getName()
                    + " is not an ISOField");

        byte[] fieldId = readBytes(in, 2);
        int fieldNumber = Integer.parseInt(new String(ISOUtil
                .ebcdicToAscii(fieldId)));

        byte[] len = readBytes(in, 2);
        int length = Integer.parseInt(new String(ISOUtil.ebcdicToAscii(len)));

        String content = ISOUtil.ebcdicToAscii(readBytes(in, length));

        ((ISOField) c).setFieldNumber(fieldNumber);
        c.setValue(content.substring(2));
    }

    public int getMaxPackedLength() {
        return getLength() + 2;
    }
}
