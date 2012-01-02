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

package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class EbcdicInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new EbcdicInterpreter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new EbcdicInterpreter().getPackedLength(100);
        assertEquals("result", 100, result);
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[2];
        new EbcdicInterpreter().interpret("", b, 100);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[1];
        try {
            new EbcdicInterpreter().interpret("testEbcdicInterpreterData", b, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        try {
            EbcdicInterpreter.INSTANCE.interpret("testEbcdicInterpreterData", (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[0];
        String result = EbcdicInterpreter.INSTANCE.uninterpret(rawData, 100, 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] rawData = new byte[0];
        try {
            new EbcdicInterpreter().uninterpret(rawData, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[2];
        try {
            new EbcdicInterpreter().uninterpret(rawData, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            new EbcdicInterpreter().uninterpret((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
