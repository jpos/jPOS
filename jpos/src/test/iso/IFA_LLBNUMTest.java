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

import org.jpos.iso.IFA_LLBNUM;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFA_LLBNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "123");
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x30340123", true);
        TestUtils.assertEquals(new byte[]{0x30, 0x34, 0x01, 0x23}, packager.pack(
                field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{0x30, 0x34, 0x01, 0x23};
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x30330123", true);
        ISOField field = new ISOField();
        packager.unpack(field, raw, 0);
        assertEquals("0123", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "123456789";
        ISOField f = new ISOField(12, origin);
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x31301234567890", false);

        ISOField unpack = new ISOField();
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin + "0", (String) unpack.getValue());
    }
}