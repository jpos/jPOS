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

/**
 * Implements BCD Interpreter. Numeric Strings (consisting of chars '0'..'9' are converted
 * to and from BCD bytes. Thus, "1234" is converted into 2 bytes: 0x12, 0x34.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class BCDInterpreter implements Interpreter
{
    /** This BCDInterpreter sometimes adds a 0-nibble to the left. */
    public static final BCDInterpreter LEFT_PADDED = new BCDInterpreter(true, false);
    /** This BCDInterpreter sometimes adds a 0-nibble to the right. */
    public static final BCDInterpreter RIGHT_PADDED = new BCDInterpreter(false, false);
    /** This BCDInterpreter sometimes adds a F-nibble to the right. */
    public static final BCDInterpreter RIGHT_PADDED_F = new BCDInterpreter(false, true);
    /** This BCDInterpreter sometimes adds a F-nibble to the left. */
    public static final BCDInterpreter LEFT_PADDED_F = new BCDInterpreter(true, true);

    private boolean leftPadded;
    private boolean fPadded;

    /** Kept private. Only two instances are possible. */
    private BCDInterpreter(boolean leftPadded, boolean fPadded) {
        this.leftPadded = leftPadded;
        this.fPadded = fPadded;
    }

    /**
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Interpreter#interpret(java.lang.String)
	 */
    public void interpret(String data, byte[] b, int offset)
    {
        ISOUtil.str2bcd(data, leftPadded, b, offset);
        if (fPadded && !leftPadded) {
            b[b.length-1] |= (byte)(b[b.length-1] << 4) == 0 ? 0x0F : 0x00;
        }
    }

    /**
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Interpreter#uninterpret(byte[])
	 */
    public String uninterpret(byte[] rawData, int offset, int length)
    {
        return ISOUtil.bcd2str (rawData, offset, length, leftPadded);
    }

    /**
	 * Each numeric digit is packed into a nibble, so 2 digits per byte, plus the
     * possibility of padding.
	 * 
	 * @see org.jpos.iso.Interpreter#getPackedLength(int)
	 */
    public int getPackedLength(int nDataUnits)
    {
        return (nDataUnits + 1) / 2;
    }
}