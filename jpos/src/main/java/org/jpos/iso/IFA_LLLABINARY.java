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
import java.text.DecimalFormat;

/**
 * ISOFieldPackager ASCII variable len BINARY
 *
 * @author salaman@teknos.com
 * @author Christopher.Harris@retail-logic.com
 * @version Id: IFA_LLLBINARY.java,v 1.0 1999/05/15 01:05 salaman Exp 
 * @see ISOComponent
 */
public class IFA_LLLABINARY extends ISOFieldPackager {
    public IFA_LLLABINARY() {
	super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLLABINARY (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
       //CJH incorrect IFA_LLLBINARY pack 08/07/04
   
        int len;
        byte[] b = (byte[]) c.getValue();
    
        if ( (len=b.length) > getLength() || len>999)
            throw new ISOException (
                "invalid len "+len 
                +" packing IFA_LLLABINARY field "+ c.getKey()
            );
        
        byte[] data = ISOUtil.hexString( (byte[]) c.getValue() ).getBytes();
        byte[] nb=new byte[ 3 +  data.length];
        byte[] length = new DecimalFormat("000").format(len).getBytes();
        System.arraycopy(length, 0, nb, 0, 3);
        System.arraycopy(data, 0, nb, 3, data.length);

        return nb;
//      CJH END
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
      //CJH incorrect IFA_LLLBINARY unpack 08/07/04
        
      int len = Integer.parseInt(new String(b, offset, 3));       
      c.setValue (ISOUtil.hex2byte(b, offset + 3, len));
      return len * 2 + 3;
        
      //CJH END
    }
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBinaryField (fieldNumber);
    }
    public int getMaxPackedLength() {
        return (getLength() << 1) + 3;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        int len = Integer.parseInt(new String(readBytes (in, 3)));
        c.setValue (readBytes (in, len));
    }
}

