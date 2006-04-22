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
 * ISOFieldPackager ASCII variable len CHAR suitable for EuroPay subfield 48<br>
 * <code>
 * Format TTLL....
 * Where TT is the 2 digit field number (Tag)
 *       LL is the 2 digit field length (value is length(data)) 
 *       .... is the field content   
 * </code>
 * 
 * @author <a href="mailto:marksalter@dsl.pipex.com">Mark Salter</a>
 * @version $Id: IFEP_TTLLCHAR.java,v 1.0 2006/04/22 09:17:00 ms Exp $
 * @see ISOComponent
 */
public class IFEP_TTLLCHAR extends ISOFieldPackager {

    String ID = getDefaultID();

    public IFEP_TTLLCHAR() {
        super();
    }

    /**
     * @param len -
     *            field len
     * @param description
     *            symbolic descrption
     */
    public IFEP_TTLLCHAR(int len, String description) {
        super(len, description);
    }

    /**
     * @param c -
     *            a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        int len;

        String s = (String) c.getValue();

        if ((len = s.length()) > getLength() || len > 97) // paranoia settings
            throw new ISOException("invalid len " + len
                    + " packing LLEPCHAR field " + (Integer) c.getKey());

        return (ID + ISOUtil.zeropad(Integer.toString(len), 2) + s).getBytes();
    }

    /**
     * @param c -
     *            the Component to unpack
     * @param b -
     *            binary image
     * @param offset -
     *            starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        // Pull ID from available stream, overidding default ID.
        ID = new String(b, offset, getIDLength());
        int len = Integer.parseInt(new String(b, offset + getIDLength(), 2));
        if (!(c instanceof ISOField))
            throw new ISOException(c.getClass().getName()
                    + " is not an ISOField");

        // ((ISOField)c).setFieldNumber (
        // Integer.parseInt(new String(b, offset+2, 2))
        // );
        c.setValue(new String(b, offset + getIDLength() + 2, len));
        return getIDLength() + 2 + len;
    }

    /*
     * @return int length of ID
     */
    int getIDLength() {
        return ID.length();
    }

    /*
     * @return String Default ID
     */
    String getDefaultID() {
        return "??";
    }

    /*
     * @param id String field ID
     */
    void setID(String id) throws ISOException {
        if (id.length() != getIDLength())
            throw new ISOException(
                    id
                            + " is not an acceptable length of field ID, it should must "
                            + getIDLength() + " long!");
        ID = id;
    }

    public void unpack(ISOComponent c, InputStream in) throws IOException,
            ISOException {

        if (!(c instanceof ISOField))
            throw new ISOException(c.getClass().getName()
                    + " is not an ISOField");

        ID = new String(readBytes(in, getIDLength()));
        int len = Integer.parseInt(new String(readBytes(in, 2)));
        int fldno = Integer.parseInt(new String(readBytes(in, 2)));
        ((ISOField) c).setFieldNumber(fldno);
        c.setValue(new String(readBytes(in, len)));
    }

    public int getMaxPackedLength() {
        return getIDLength() + getLength() + 2;
    }
}
