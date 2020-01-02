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

/*
 * vim:set ts=8 sw=4:
 */

/**
 * ISOFieldPackager ASCII variable len CHAR 
 * suitable for EuroPay subfield 48<br>
 * <code>
 * Format LLTT....
 * Where LL is the 2 digit field length
 *       TT is the 2 digit field number (Tag)
 *       is the field content   
 * </code>
 *
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @version $Id$
 * @see ISOComponent
 */
public class IFEP_LLCHAR extends ISOFieldPackager {
    public IFEP_LLCHAR() {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFEP_LLCHAR (int len, String description) {
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
    
        if ((len=s.length()) > getLength() || len>97)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing IFEP_LLCHAR field "
                + c.getKey()
            );

        return (
            ISOUtil.zeropad(Integer.toString(len+2), 2) 
           +ISOUtil.zeropad(c.getKey().toString(), 2)
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
        int len = Integer.parseInt(new String(b, offset, 2));
        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");

        c.setFieldNumber(
                Integer.parseInt(new String(b, offset + 2, 2))
        );
        c.setValue (new String (b, offset+4, len-2));
        return len + 2;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {

        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");

        int len   = Integer.parseInt(new String(readBytes (in, 2)));
        int fldno = Integer.parseInt(new String(readBytes (in, 2)));
        c.setFieldNumber(fldno);
        c.setValue (new String (readBytes (in, len-2)));
    }

    public int getMaxPackedLength() {
        return getLength() + 2;
    }
}
