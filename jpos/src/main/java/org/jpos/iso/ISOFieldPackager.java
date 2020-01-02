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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;

/**
 * base class for the various IF*.java Field Packagers
 * Implements "FlyWeight" pattern
 *
 * @author apr@cs.com.uy
 * @version $Id$
 *
 * @see IFA_AMOUNT
 * @see IFA_BINARY
 * @see IFA_BITMAP
 * @see IFA_FLLCHAR
 * @see IFA_FLLNUM
 * @see IFA_LLCHAR
 * @see IFA_LLLBINARY
 * @see IFA_LLLCHAR
 * @see IFA_LLLNUM
 * @see IFA_LLNUM
 * @see IFA_NUMERIC
 * @see IFB_AMOUNT
 * @see IFB_BINARY
 * @see IFB_BITMAP
 * @see IFB_LLBINARY
 * @see IFB_LLCHAR
 * @see IFB_LLHBINARY
 * @see IFB_LLHCHAR
 * @see IFB_LLHECHAR
 * @see IFB_LLHNUM
 * @see IFB_LLLBINARY
 * @see IFB_LLLCHAR
 * @see IFB_LLLNUM
 * @see IFB_LLNUM
 * @see IFB_NUMERIC
 * @see IF_CHAR
 */
public abstract class ISOFieldPackager {
    private int len;
    private String description;
    protected boolean pad;
    protected boolean trim;

    /**
     * Default Constructor
     */
    public ISOFieldPackager()
    {
        this.len = -1;
        this.description = null;
    }

    /**
     * @param len - field Len
     * @param description - details
     */
    public ISOFieldPackager(int len, String description) {
        this.len = len;
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getLength() {
        return len;
    }
    public void setLength(int len) {
        this.len = len;
    }

    public void setPad(boolean pad) {
        this.pad = pad;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public abstract int getMaxPackedLength();

    public ISOComponent createComponent(int fieldNumber) {
        return new ISOField (fieldNumber);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public abstract byte[] pack (ISOComponent c) throws ISOException;

    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public abstract int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException;

    /**
     * @param c  - the Component to unpack
     * @param in - input stream
     * @exception ISOException
     */
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        unpack (c, readBytes (in, getMaxPackedLength ()), 0);
    }
    /**
     * @param c   - the Component to unpack
     * @param out - output stream
     * @exception ISOException
     * @exception IOException
     */
    public void pack (ISOComponent c, ObjectOutput out) 
        throws IOException, ISOException
    {
        out.write (pack (c));
    }

    protected byte[] readBytes (InputStream in, int l) throws IOException {
        byte[] b = new byte [l];
        int n = 0;
        while (n < l) {
            int count = in.read(b, n, l - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
        return b;
    }
}

