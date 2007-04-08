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

package iso;

import junit.framework.TestCase;

import org.jpos.iso.AsciiHexInterpreter;


/**
 * @author joconnor
 */
public class AsciiHexInterpreterTest extends TestCase {
    private AsciiHexInterpreter inter;

    /*
	 * @see TestCase#setUp()
	 */
    protected void setUp() throws Exception {
        inter = AsciiHexInterpreter.INSTANCE;
    }

    public void testInterpret() {
        byte[] data = new byte[] {(byte)0xFF, (byte)0x12};
        byte[] b = new byte[4];
        inter.interpret(data, b, 0);
        TestUtils.assertEquals(new byte[] {0x46, 0x46, 0x31, 0x32}, b);
    }

    public void testUninterpret() {
        byte[] data = new byte[] {(byte)0xFF, (byte)0x12};
        byte[] b = new byte[] {0x46, 0x46, 0x31, 0x32};
        TestUtils.assertEquals(data, inter.uninterpret(b, 0, 2));
    }

    public void testGetPackedLength() {
        assertEquals(6, inter.getPackedLength(3));
    }
    
    public void testReversability() {
        byte data[] = new byte[] {0x01, 0x23, 0x45, 0x67, (byte)0x89,
                (byte)0xAB, (byte)0xCD, (byte)0xEF};
        byte[] b = new byte[inter.getPackedLength(data.length)];
        inter.interpret(data, b, 0);
        
        TestUtils.assertEquals(data, inter.uninterpret(b, 0, data.length));
    }
}