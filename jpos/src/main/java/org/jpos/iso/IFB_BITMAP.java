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
import java.util.BitSet;

/**
 * ISOFieldPackager Binary Bitmap
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class IFB_BITMAP extends ISOBitMapPackager {
    public IFB_BITMAP() {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_BITMAP(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        BitSet b = (BitSet) c.getValue();
        int len =                                           // bytes needed to encode BitSet (in 8-byte chunks)
            getLength() >= 8 ?
                    b.length()+62 >>6 <<3 : getLength();    // +62 because we don't use bit 0 in the BitSet
        return ISOUtil.bitSet2byte (b, len);
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
        int len;
        BitSet bmap = ISOUtil.byte2BitSet (b, offset, getLength() << 3);
        c.setValue(bmap);
        len = bmap.get(1) ? 128 : 64;
        if (getLength() > 16 && bmap.get(1) && bmap.get(65))
            len = 192;
        return Math.min (getLength(), len >> 3);
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        BitSet bmap = ISOUtil.byte2BitSet (new BitSet (64), readBytes (in, 8), 0);
        if (getLength() > 8 && bmap.get (1)) {
            ISOUtil.byte2BitSet (bmap, readBytes (in, 8), 64); 
        }
        if (getLength() > 16 && bmap.get (65)) {
            ISOUtil.byte2BitSet (bmap, readBytes (in, 8), 128); 
        }
        c.setValue(bmap);
    }
    public int getMaxPackedLength() {
        return getLength() >> 3;
    }
}
