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
 * Implements Hex Interpreter. The Hex digits are stored in ASCII.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class AsciiHexInterpreter implements BinaryInterpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final AsciiHexInterpreter INSTANCE = new AsciiHexInterpreter();

    /** 0-15 to ASCII hex digit lookup table. */
    private static final byte[] HEX_ASCII = new byte[] {
              0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
              0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46
    };

    /**
     * Converts the binary data into ASCII hex digits.
     * @see org.jpos.iso.BinaryInterpreter#interpret(byte[], byte[], int)
     */
    public void interpret(byte[] data, byte[] b, int offset)
    {
        for (int i = 0; i < data.length; i++) {
            b[offset + i * 2] = HEX_ASCII[(data[i] & 0xF0) >> 4];
            b[offset + i * 2 + 1] = HEX_ASCII[data[i] & 0x0F];
        }
    }

    /**
     * Converts the ASCII hex digits into binary data.
     * @see org.jpos.iso.BinaryInterpreter#uninterpret(byte[], int, int)
     */
    public byte[] uninterpret(byte[] rawData, int offset, int length)
    {
        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++)
        {
            byte hi = rawData[offset + i * 2];
            byte lo = rawData[offset + i * 2 + 1];
            int h = hi > 0x40 ? 10 + hi - 0x41 : hi - 0x30;
            int l = lo > 0x40 ? 10 + lo - 0x41 : lo - 0x30;
            ret[i] = (byte)(h << 4 | l);
        }
        return ret;
    }

    /**
     * Returns double nBytes because the hex representation of 1 byte needs 2 hex digits.
     * 
     * @see org.jpos.iso.BinaryInterpreter#getPackedLength(int)
     */
    public int getPackedLength(int nBytes)
    {
        return nBytes * 2;
    }
}