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
import java.io.IOException;
import java.io.InputStream;

/**
 * EBCDIC version of IFB_LLNUM
 * Uses a 2 EBCDIC byte length field
 *
 * based on Eoin's IFE_LLCHAR
 * @author apr@cs.com.uy
 * @author Jonathan.O'Connor@xcom.de
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LLNUM extends ISOFieldPackager 
{
    public IFE_LLNUM () {
        super();
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLNUM (int len, String description) {
        super (len, description);
    }
    /**
    * @param c - a component
    * @return packed component
    * @exception ISOException
    */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len;
        String s = (String) c.getValue();
    
        if ((len=s.length()) > getLength() || len>99)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing LLNUM field "+(Integer) c.getKey()
            );

        byte[] ebcdic = ISOUtil.asciiToEbcdic( s );
        byte[] b   = new byte[ebcdic.length + 2];
        byte[] l   = ISOUtil.asciiToEbcdic(
            ISOUtil.zeropad (Integer.toString (len), 2)
        );
        System.arraycopy(l, 0, b, 0, l.length);
        System.arraycopy(ebcdic, 0, b, 2, ebcdic.length);
        return b;
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
        int len = Integer.parseInt (ISOUtil.ebcdicToAscii(b, offset, 2));
        c.setValue (ISOUtil.ebcdicToAscii(b, offset + 2, len));
        return 2 + len;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        int len = Integer.parseInt (ISOUtil.ebcdicToAscii(readBytes (in, 2)));
        c.setValue(ISOUtil.ebcdicToAscii(readBytes (in, len)));
    }
    public int getMaxPackedLength() {
        return getLength()+2;
    }
}
