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

public class SignedEbcdicNumberInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new SignedEbcdicNumberInterpreter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new SignedEbcdicNumberInterpreter().getPackedLength(100);
        assertEquals("result", 100, result);
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new SignedEbcdicNumberInterpreter().getPackedLength(0);
        assertEquals("result", 0, result);
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] targetArray = new byte[1];
        new SignedEbcdicNumberInterpreter().interpret("", targetArray, 100);
        assertEquals("targetArray.length", 1, targetArray.length);
    }

    @Test
    public void testInterpret1() throws Throwable {
        byte[] targetArray = new byte[3];
        new SignedEbcdicNumberInterpreter().interpret("-\u5483\uD0AF", targetArray, 0);
        assertEquals("targetArray[0]", (byte) 35, targetArray[0]);
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] targetArray = new byte[2];
        try {
            new SignedEbcdicNumberInterpreter().interpret("testSignedEbcdicNumberInterpreterData", targetArray, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
            assertEquals("targetArray.length", 2, targetArray.length);
        }
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] targetArray = new byte[1];
        try {
            new SignedEbcdicNumberInterpreter().interpret("-\u5483\uD0AF", targetArray, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
            assertEquals("targetArray.length", 1, targetArray.length);
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        try {
            new SignedEbcdicNumberInterpreter().interpret("-", (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException1() throws Throwable {
        byte[] targetArray = new byte[1];
        try {
            new SignedEbcdicNumberInterpreter().interpret(null, targetArray, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("targetArray.length", 1, targetArray.length);
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[3];
        String result = new SignedEbcdicNumberInterpreter().uninterpret(rawData, 0, 1);
        assertEquals("rawData[0]", (byte) -16, rawData[0]);
        assertEquals("result", "0", result);
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] rawData = new byte[2];
        rawData[0] = (byte) -48;
        try {
            new SignedEbcdicNumberInterpreter().uninterpret(rawData, -48, 49);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("rawData[0]", (byte) -16, rawData[0]);
            assertEquals("ex.getMessage()", "-48", ex.getMessage());
            assertEquals("rawData.length", 2, rawData.length);
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] rawData = new byte[3];
        try {
            new SignedEbcdicNumberInterpreter().uninterpret(rawData, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1099", ex.getMessage());
            assertEquals("rawData.length", 3, rawData.length);
        }
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[3];
        rawData[0] = (byte) -39;
        try {
            new SignedEbcdicNumberInterpreter().uninterpret(rawData, 240, -239);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertEquals("rawData[0]", (byte) -7, rawData[0]);
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("rawData.length", 3, rawData.length);
        }
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            new SignedEbcdicNumberInterpreter().uninterpret((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
