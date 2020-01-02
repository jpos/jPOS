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
 * Similar to Europay format, but instead of LLTT it's TTTTLLL
 * <code>
 * Format TTLL....
 * Where 
 *       TTTT is the 4 digit field number (Tag)
 *       LLL is the 3 digit field length
 *       .. is the field content   
 * </code>
 *
 * @author Alejandro Revilla
 * @version $Id$
 * @see IFEP_LLCHAR
 */
public class IFIPM_LLLCHAR extends ISOFieldPackager {
    public IFIPM_LLLCHAR () {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFIPM_LLLCHAR (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len;
        String s = (String) c.getValue();
    
        if ((len=s.length()) > getLength() || len>997)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing IFIPM_LLLCHAR field "
                + c.getKey() + " maxlen=" + getLength()
            );

        return (
            ISOUtil.zeropad(c.getKey().toString(), 4)
           +ISOUtil.zeropad(Integer.toString(len), 3) 
           +s
        ).getBytes();
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
        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");
        
        c.setFieldNumber(
                Integer.parseInt(new String(b, offset, 4))
        );
        offset += 4;
        int len = Integer.parseInt(new String(b, offset, 3));
        offset += 3;
        c.setValue (new String (b, offset, len));
        return len + 7;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {

        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");

        int fldno = Integer.parseInt(new String(readBytes (in, 4)));
        int len   = Integer.parseInt(new String(readBytes (in, 3)));
        c.setFieldNumber(fldno);
        c.setValue (new String (readBytes (in, len)));
    }

    public int getMaxPackedLength() {
        return getLength() + 7;
    }
}

