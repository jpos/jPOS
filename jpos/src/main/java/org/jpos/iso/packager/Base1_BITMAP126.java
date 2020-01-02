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

package org.jpos.iso.packager;

import org.jpos.iso.ISOBitMapPackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

import java.util.BitSet;

/**
 * ISOFieldPackager Binary Bitmap
 *
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class Base1_BITMAP126 extends ISOBitMapPackager 
{
    public Base1_BITMAP126()
    {
        super();
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public Base1_BITMAP126(int len, String description) 
    {
        super(len, description);
    }
    /**
    * @param c - a component
    * @return packed component
    * @exception ISOException
    */
    public byte[] pack (ISOComponent c) throws ISOException 
    {
        return ISOUtil.bitSet2byte ((BitSet) c.getValue());
    }
    /**
    * @param c - the Component to unpack
    * @param b - binary image
    * @param offset - starting offset within the binary image
    * @return consumed bytes
    * @exception ISOException
    */
    public int unpack (ISOComponent c, byte[] b, int offset) throws ISOException
    {
        int len;
        // 
        // For this type of Bitmap bit0 does not mean
        // that there is a secondary bitmap to follow
        // It simply means that field 1 is present
        // The standard IFB_BITMAP class assumes that
        // bit0 always means extended bitmap 
        //
        BitSet bmap = ISOUtil.byte2BitSet (b, offset, false); // False => no extended bitmap

        c.setValue(bmap);
        len = (len=bmap.size()) > 128 ? 128 : len;            // BBB I think we want bmap.length(), but nobody is complaining...
        return len >> 3;
    }
    public int getMaxPackedLength() 
    {
        return getLength() >> 3;
    }
}
