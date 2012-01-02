/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.BitSet;

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOMsg;
import org.junit.Test;

public class Base1_BITMAP126Test {

    @Test
    public void testConstructor() throws Throwable {
        Base1_BITMAP126 base1_BITMAP126 = new Base1_BITMAP126();
        assertNull("base1_BITMAP126.getDescription()", base1_BITMAP126.getDescription());
        assertEquals("base1_BITMAP126.getLength()", -1, base1_BITMAP126.getLength());
    }

    @Test
    public void testConstructor1() throws Throwable {
        Base1_BITMAP126 base1_BITMAP126 = new Base1_BITMAP126(100, "testBase1_BITMAP126Description");
        assertEquals("base1_BITMAP126.getDescription()", "testBase1_BITMAP126Description", base1_BITMAP126.getDescription());
        assertEquals("base1_BITMAP126.getLength()", 100, base1_BITMAP126.getLength());
    }

    @Test
    public void testGetMaxPackedLength() throws Throwable {
        int result = new Base1_BITMAP126(100, "testBase1_BITMAP126Description").getMaxPackedLength();
        assertEquals("result", 12, result);
    }

    @Test
    public void testGetMaxPackedLength1() throws Throwable {
        int result = new Base1_BITMAP126(0, "testBase1_BITMAP126Description").getMaxPackedLength();
        assertEquals("result", 0, result);
    }

    @Test
    public void testPack() throws Throwable {
        BitSet v = new BitSet(100);
        Base1_BITMAP126 base1_BITMAP126 = new Base1_BITMAP126(100, "testBase1_BITMAP126Description");
        byte[] result = base1_BITMAP126.pack(new ISOBitMap(100, v));
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testPackThrowsClassCastException() throws Throwable {
        try {
            new Base1_BITMAP126(100, "testBase1_BITMAP126Description").pack(new ISOMsg("testBase1_BITMAP126Mti"));
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
        }
    }

    @Test
    public void testPackThrowsNullPointerException() throws Throwable {
        try {
            new Base1_BITMAP126(100, "testBase1_BITMAP126Description").pack(new ISOBinaryField());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpackThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[0];
        ISOComponent c = new ISOMsg();
        try {
            new Base1_BITMAP126().unpack(c, b, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
            assertEquals("(ISOMsg) c.getDirection()", 0, ((ISOMsg) c).getDirection());
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException() throws Throwable {
        Base1_BITMAP126 base1_BITMAP126 = new Base1_BITMAP126(100, "testBase1_BITMAP126Description");
        ISOComponent c = new ISOBinaryField(100);
        try {
            base1_BITMAP126.unpack(c, (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(ISOBinaryField) c.getBytes()", ((ISOBinaryField) c).getBytes());
        }
    }
}
