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
 * Length is represented in ASCII (as in IFA_LL*)
 * Value is represented in BCD
 * ISOFieldPackager Binary LLNUM
 *
 * @author Mladen Mrkic <mmrkic@arius.co.yu>
 *
 * @see ISOComponent
 */
public class IFA_LLBNUM extends ISOFieldPackager {
    private Interpreter interpreter;
    private Prefixer prefixer;
    
    public IFA_LLBNUM () {
        super();
        interpreter = BCDInterpreter.LEFT_PADDED;
        prefixer = AsciiPrefixer.LL;
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public  IFA_LLBNUM (int len, String description, boolean pad) {
        super(len, description);
        this.pad = pad;
        interpreter = pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED;
        prefixer = AsciiPrefixer.LL;
    }

    public void setPad(boolean pad)
    {
        this.pad = pad;
        interpreter = pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED;
    }

    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        String s = (String) c.getValue();
        int len = s.length();
        if (len > getLength() || len>99)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing LLNUMAB field " + c.getKey()
            );

        byte[] b = new byte[3 + (len >> 1)];
        prefixer.encodeLength(((len + 1) >> 1) << 1, b);
        interpreter.interpret(s, b, 2);
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
        int len = prefixer.decodeLength(b, offset);
        c.setValue (interpreter.uninterpret(b, offset + 2, len));
        return 2 + (++len >> 1);
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        int len = prefixer.decodeLength(readBytes (in, 2), 2);
        c.setValue (interpreter.uninterpret(readBytes (in, (len+2) >> 1), 0, len));
    }
    public int getMaxPackedLength() {
        return 1 + ((getLength()+1) >> 1);
    }
}
