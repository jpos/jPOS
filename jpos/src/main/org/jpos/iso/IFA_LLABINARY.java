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
import java.text.DecimalFormat;

/**
 * ISOFieldPackager ASCII variable len BINARY
 *
 * @author apr@cs.com.uy
 * @author Christopher.Harris@retail-logic.com
 * @version Id: $
 * @see ISOComponent
 */
public class IFA_LLABINARY extends ISOFieldPackager {
    public IFA_LLABINARY() {
	super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLABINARY (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len;
        byte[] b = (byte[]) c.getValue();
    
        if ( (len=b.length) > getLength() || len>99)
            throw new ISOException (
                "invalid len "+len 
                +" packing field "+(Integer) c.getKey()
            );
        //CJH incorrect IFA_LLBINARY pack 08/07/04
        byte[] data = ISOUtil.hexString( (byte[]) c.getValue() ).getBytes();
        byte[] nb=new byte[ 2 +  data.length];
        
        byte[] length = new DecimalFormat("00").format(len).getBytes();
        System.arraycopy(length, 0, nb, 0, 2);
        System.arraycopy(data, 0, nb, 2, data.length);
        return nb;
        //CJH END.
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
      //CJH incorrect IFA_LLBINARY unpack 08/07/04
         
      int len = Integer.parseInt(new String(b, offset, 2));       
      c.setValue (ISOUtil.hex2byte(b, offset + 2, len));
      return (len * 2) + 2;

      //CJH END.
        
        
    }
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBinaryField (fieldNumber);
    }
    public int getMaxPackedLength() {
        return (getLength() << 1) + 2;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        int len = Integer.parseInt(new String(readBytes (in, 2)));
        c.setValue (readBytes (in, len));
    }
}

