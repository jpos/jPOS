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

import org.jpos.iso.BcdPrefixer;

/**
 * Tests the ASCII length Prefixer.
 * @author jonathan.oconnor@xcom
 */
public class BcdPrefixerTest extends TestCase
{
    public void testEncode() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(21, b);
        TestUtils.assertEquals(new byte[]{0x21}, b);
    }

    public void testEncodeShortLength() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x03}, b);
    }

    public void testEncodeLLL() throws Exception
    {
        byte[] b = new byte[2];
        BcdPrefixer.LLL.encodeLength(321, b);
        TestUtils.assertEquals(new byte[]{0x03, 0x21}, b);
    }

    public void testEncodeLLLShortLength() throws Exception
    {
        byte[] b = new byte[2];
        BcdPrefixer.LLL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x00, 0x03}, b);
    }

    public void testEncode99() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(99, b);
        TestUtils.assertEquals(new byte[]{(byte)0x99}, b);
    }

    public void testDecode() throws Exception
    {
        byte[] b = new byte[]{0x25};
        assertEquals(25, BcdPrefixer.LL.decodeLength(b, 0));
    }

    public void testDecode19() throws Exception
    {
        byte[] b = new byte[]{0x19};
        assertEquals(19, BcdPrefixer.LL.decodeLength(b, 0));
    }

    public void testDecode99() throws Exception
    {
        byte[] b = new byte[]{(byte)0x99};
        assertEquals(99, BcdPrefixer.LL.decodeLength(b, 0));
    }

    public void testReversability() throws Exception
    {
        int len = 3;
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(len, b);
        assertEquals(len, BcdPrefixer.LL.decodeLength(b, 0));
    }
}