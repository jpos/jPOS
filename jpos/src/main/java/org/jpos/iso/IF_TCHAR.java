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
 * ISOFieldPackager CHARACTERS (ASCII and BINARY)
 * deal fields terminated by special token
 * @author Zhiyu Tang
 * @version $Id$
 * @see ISOComponent
 */
public class IF_TCHAR extends IF_TBASE {
    public IF_TCHAR() {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IF_TCHAR(int len, String description) {
        super(len, description);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     * @param token token descrption
     */
    public IF_TCHAR(int len, String description, String token) {
        super(len, description, token);
    }

    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    @Override
    public byte[] pack (ISOComponent c) throws ISOException {
        String s = c.getValue() + getToken() ;
        return s.getBytes(ISOUtil.CHARSET);
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    @Override
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        String s = new String(b, ISOUtil.CHARSET);
        int newoffset = s.indexOf( getToken() , offset );
        c.setValue( s.substring(offset, newoffset ));
        int len = newoffset - offset;
        setLength( len );
        return len + getToken().length();
    }

    @Override
    public int getMaxPackedLength() {
        return getLength() + getToken().length();
    }
}
