/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
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
 * @see IF_ECHAR
 */
public abstract class ISOFieldPackager {
    private int len;
    private String description;
    protected boolean pad;

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

