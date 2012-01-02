/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
 * ISOMsgFieldPackager is a packager able to pack compound ISOMsgs
 * (one message inside another one, and so on...)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see org.jpos.iso.packager.PostPackager
 */
public class ISOMsgFieldPackager extends ISOFieldPackager {
    protected ISOPackager msgPackager;
    protected ISOFieldPackager fieldPackager;

    /**
     * @param fieldPackager low level field packager
     * @param msgPackager ISOMsgField default packager
     */
    public ISOMsgFieldPackager (
            ISOFieldPackager fieldPackager,
            ISOPackager msgPackager)
    {
        super(fieldPackager.getLength(), fieldPackager.getDescription());
        this.msgPackager = msgPackager;
        this.fieldPackager = fieldPackager;
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        if (c instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) c;
            m.recalcBitMap();
            ISOBinaryField f = new ISOBinaryField(0, msgPackager.pack(m));
            return fieldPackager.pack(f);
        }
        return fieldPackager.pack(c);
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
        ISOBinaryField f = new ISOBinaryField(0);
        int consumed = fieldPackager.unpack(f, b, offset);
        if (c instanceof ISOMsg) 
            msgPackager.unpack((ISOMsg) c, (byte[]) f.getValue());
        return consumed;
    }

    /**
     * @param c  - the Component to unpack
     * @param in - input stream
     * @exception ISOException
     */
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        ISOBinaryField f = new ISOBinaryField(0);
        fieldPackager.unpack (f, in);
        if (c instanceof ISOMsg) {
            msgPackager.unpack((ISOMsg) c, (byte[]) f.getValue());
        }
    }
    public ISOComponent createComponent(int fieldNumber) {
        ISOMsg m = new ISOMsg(fieldNumber);
        m.setPackager(msgPackager);
        return m;
    }
    public int getMaxPackedLength() {
        return fieldPackager.getLength();
    }
    public ISOPackager getISOMsgPackager() {
        return msgPackager;
    }
    public ISOFieldPackager getISOFieldPackager() {
        return fieldPackager;
    }
}
