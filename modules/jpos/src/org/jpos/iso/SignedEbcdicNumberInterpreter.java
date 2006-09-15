/*
 * Copyright (c) 2000 jPOS.org. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The end-user documentation included with the redistribution, if any,
 * must include the following acknowledgment: "This product includes software
 * developed by the jPOS project (http://www.jpos.org/)". Alternately, this
 * acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear.
 *  4. The names "jPOS" and "jPOS.org" must not be used to endorse or promote
 * products derived from this software without prior written permission. For
 * written permission, please contact license@jpos.org.
 *  5. Products derived from this software may not be called "jPOS", nor may
 * "jPOS" appear in their name, without prior written permission of the jPOS
 * project.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JPOS
 * PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the jPOS Project. For more information please see
 * <http://www.jpos.org/> .
 */

package org.jpos.iso;

/**
 * Implements EBCDIC Interpreter for signed numerics.
 * (see http://publib.boulder.ibm.com/infocenter/wmbhelp/v6r0m0/index.jsp?topic=/com.ibm.etools.mft.doc/ad06900_.htm)
 * Strings are converted to and from EBCDIC bytes.
 * Negatives will be prepended with "-"
 * Unsigned numbers are interpreted as positive
 * 
 * @author nsmith@mxgroup.net
 * @version $Revision$ $Date$
 */
public class SignedEbcdicNumberInterpreter implements Interpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final SignedEbcdicNumberInterpreter INSTANCE = new SignedEbcdicNumberInterpreter();

    public void interpret(String data, byte[] targetArray, int offset) {
        boolean negative = data.startsWith("-");
        if (negative) {
            ISOUtil.asciiToEbcdic(data.substring(1), targetArray, offset);
            targetArray[offset + data.length() - 2] = (byte) (targetArray[offset + data.length() - 2] & 0xDF); 
        } else {
            ISOUtil.asciiToEbcdic(data, targetArray, offset);
        }
    }

    public String uninterpret(byte[] rawData, int offset, int length) {
        boolean negative = ((byte)(rawData[offset + length - 1] & 0xF0)) == ((byte)0xD0);
        rawData[offset + length - 1] = ((byte)(rawData[offset + length - 1] | 0xF0));
        return (negative ? "-" : "") + ISOUtil.ebcdicToAscii(rawData, offset, length);
    }

    public int getPackedLength(int nDataUnits)
    {
        return nDataUnits;
    }
}