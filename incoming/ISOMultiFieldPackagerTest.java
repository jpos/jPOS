/*
 * Copyright (c) 2005 jPOS.org. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import junit.framework.TestCase;

import org.jpos.iso.IFB_LLNUM;
import org.jpos.iso.IFE_LLNUM;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.packager.ISOMultiFieldPackager;
import iso.TestUtils;

/**
 * @author Mark Salter
 */
public class ISOMultiFieldPackagerTest extends TestCase {
	
	public void testPackList() throws Exception {
		Vector list = new Vector();
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFE_LLNUM(10, "Should be 041234"));
		list.add(new IFB_LLNUM(10, "The one to pack", true));

		ISOMultiFieldPackager packager = new ISOMultiFieldPackager(
				"Should be 041234", list);
		
		packager.hint("The one to pack");

		ISOField field = new ISOField(12, "1234");

		TestUtils.assertEquals(new byte[] { (byte) 0x04, (byte) 0x12,
				(byte) 0x34 }, packager.pack(field));
	}
	
	
	public void testPackArray() throws Exception {

		ISOMultiFieldPackager packager = new ISOMultiFieldPackager(
				"Should be 041234", new ISOFieldPackager[] {
						new IFB_LLNUM(10, "Should not be this", true),
						new IFE_LLNUM(10, "Should be 041234"),
						new IFB_LLNUM(10, "The one to pack", true) });
		
		packager.hint("The one to pack");

		ISOField field = new ISOField(12, "1234");

		TestUtils.assertEquals(new byte[] { (byte) 0x04, (byte) 0x12,
				(byte) 0x34 }, packager.pack(field));
	}

	public void testPackNoHintFail() throws Exception {

		ISOMultiFieldPackager packager = new ISOMultiFieldPackager(
				"Should be 041234", new ISOFieldPackager[] {
						new IFB_LLNUM(10, "Should not be this", true),
						new IFE_LLNUM(10, "Should be 041234"),
						new IFB_LLNUM(10, "The one to pack", true) });
		// packager.hint("The one to pack");

		ISOField field = new ISOField(12, "1234");

		try {
			TestUtils.assertEquals(new byte[] { (byte) 0x04, (byte) 0x12,
					(byte) 0x34 }, packager.pack(field));
			fail("pack without hint should fail with ISOException!");
		} catch (Exception expected) {
			// Expected!
		}
	}

	public void testUnpackList() throws Exception {
		byte[] raw = new byte[] { (byte) 0xF0, (byte) 0xF4, (byte) 0xF1,
				(byte) 0xF2, (byte) 0xF3, (byte) 0xF4 };

		Vector list = new Vector();
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFB_LLNUM(10, "Should not be this", true));
		list.add(new IFE_LLNUM(10, "Should be 041234"));
		list.add(new IFB_LLNUM(10, "Should not be this", true));

		ISOMultiFieldPackager packager = new ISOMultiFieldPackager(
				"A List choice", list);

		ISOField field = new ISOField();
		packager.unpack(field, raw, 0);

		assertEquals("1234", (String) field.getValue());
	}

	public void testUnpackArray() throws Exception {
		byte[] raw = new byte[] { (byte) 0xF0, (byte) 0xF4, (byte) 0xF1,
				(byte) 0xF2, (byte) 0xF3, (byte) 0xF4 };

		ISOMultiFieldPackager packager = new ISOMultiFieldPackager(
				"An Array choice", new ISOFieldPackager[] {
						new IFB_LLNUM(10, "Should not be this", true),
						new IFE_LLNUM(10, "Should be 041234"),
						new IFB_LLNUM(10, "Should not be this", true) });

		ISOField field = new ISOField();
		packager.unpack(field, raw, 0);

		assertEquals("1234", (String) field.getValue());

	}
	
	public void testUnpackArrayfromInputStream() throws Exception {
		byte[] raw = new byte[] { (byte) 0xF0, (byte) 0xF4, (byte) 0xF1,
				(byte) 0xF2, (byte) 0xF3, (byte) 0xF4 };
		
		ISOMultiFieldPackager packager = new ISOMultiFieldPackager(
				"An Array choice", new ISOFieldPackager[] {
						new IFB_LLNUM(10, "Should not be this", true),
						new IFE_LLNUM(10, "Should be 041234"),
						new IFB_LLNUM(10, "Should not be this", true) });

		ISOField field = new ISOField();
		packager.unpack(field, (InputStream)new ByteArrayInputStream(raw));

		assertEquals("1234", (String) field.getValue());

	}

	// public void testReversability() throws Exception
	// {
	// String origin = "1234567890";
	// ISOField f = new ISOField(12, origin);
	// IFE_LLNUM packager = new IFE_LLNUM(10, "Should be 1234567890");
	//
	// ISOField unpack = new ISOField(12);
	// packager.unpack(unpack, packager.pack(f), 0);
	// assertEquals(origin, (String) unpack.getValue());
	// }
}