/*
 * Copyright (c) 2000 jPOS.org. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowledgment: "This product includes software developed by the jPOS
 * project (http://www.jpos.org/)". Alternately, this acknowledgment may appear
 * in the software itself, if and wherever such third-party acknowledgments
 * normally appear. 4. The names "jPOS" and "jPOS.org" must not be used to
 * endorse or promote products derived from this software without prior written
 * permission. For written permission, please contact license@jpos.org. 5.
 * Products derived from this software may not be called "jPOS", nor may "jPOS"
 * appear in their name, without prior written permission of the jPOS project.
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

import org.jpos.iso.IFA_BINARY;
import org.jpos.iso.ISOBinaryField;

/**
 * @author joconnor
 */
public class IFA_BINARYTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x12, 0x34});
        IFA_BINARY packager = new IFA_BINARY(2, "Should be 1234");
        TestUtils.assertEquals("1234".getBytes(), packager.pack(field));
    }

    public void testPackWrongLength() throws Exception
    {
        try
        {
            ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x12, 0x34});
            IFA_BINARY packager = new IFA_BINARY(3, "Should be 1234");
            packager.pack(field);
            fail("Packing 2 bytes into 3 should throw an exception");
        }
        catch (Exception ignored)
        {
            // Exception expected - correct behaviour
        }
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "1234".getBytes();
        IFA_BINARY packager = new IFA_BINARY(2, "Should be 1234");
        ISOBinaryField field = new ISOBinaryField(12);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x12, 0x34}, (byte[])field.getValue());
    }

    public void testReversability() throws Exception
    {
        byte[] origin = new byte[] {0x12, 0x34, 0x56, 0x78};
        ISOBinaryField f = new ISOBinaryField(12, origin);
        IFA_BINARY packager = new IFA_BINARY(4, "Should be 12345678");

        ISOBinaryField unpack = new ISOBinaryField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        TestUtils.assertEquals(origin, (byte[])unpack.getValue());
    }
}