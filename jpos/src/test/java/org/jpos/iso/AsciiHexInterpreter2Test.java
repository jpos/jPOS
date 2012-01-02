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

public class AsciiHexInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new AsciiHexInterpreter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = AsciiHexInterpreter.INSTANCE.getPackedLength(0);
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new AsciiHexInterpreter().getPackedLength(100);
        assertEquals("result", 200, result);
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[1];
        byte[] data = new byte[0];
        new AsciiHexInterpreter().interpret(data, b, 100);
        assertEquals("b.length", 1, b.length);
    }

    @Test
    public void testInterpret1() throws Throwable {
        byte[] data = new byte[1];
        byte[] b = new byte[5];
        AsciiHexInterpreter.INSTANCE.interpret(data, b, 0);
        assertEquals("b[0]", (byte) 48, b[0]);
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] data = new byte[4];
        byte[] b = new byte[1];
        try {
            AsciiHexInterpreter.INSTANCE.interpret(data, b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("b[0]", (byte) 48, b[0]);
            assertEquals("ex.getMessage()", "1", ex.getMessage());
            assertEquals("b.length", 1, b.length);
        }
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] data = new byte[1];
        byte[] b = new byte[0];
        try {
            AsciiHexInterpreter.INSTANCE.interpret(data, b, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
            assertEquals("b.length", 0, b.length);
        }
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[3];
        byte[] data = new byte[2];
        try {
            AsciiHexInterpreter.INSTANCE.interpret(data, b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("b[0]", (byte) 48, b[0]);
            assertEquals("ex.getMessage()", "3", ex.getMessage());
            assertEquals("b.length", 3, b.length);
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        byte[] b = new byte[5];
        try {
            new AsciiHexInterpreter().interpret((byte[]) null, b, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("b.length", 5, b.length);
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[70];
        rawData[65] = (byte) 65;
        byte[] result = AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 63, 3);
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) -48, result[0]);
    }

    @Test
    public void testUninterpret1() throws Throwable {
        byte[] rawData = new byte[5];
        rawData[3] = (byte) 64;
        rawData[4] = (byte) 64;
        byte[] result = AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 3, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 16, result[0]);
    }

    @Test
    public void testUninterpret2() throws Throwable {
        byte[] rawData = new byte[67];
        rawData[66] = (byte) 65;
        byte[] result = AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 65, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 10, result[0]);
    }

    @Test
    public void testUninterpret3() throws Throwable {
        byte[] rawData = new byte[74];
        rawData[66] = (byte) 65;
        byte[] result = AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 65, 3);
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) 10, result[0]);
    }

    @Test
    public void testUninterpret4() throws Throwable {
        byte[] rawData = new byte[67];
        rawData[65] = (byte) 65;
        rawData[66] = (byte) 65;
        byte[] result = AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 65, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) -86, result[0]);
    }

    @Test
    public void testUninterpret5() throws Throwable {
        byte[] rawData = new byte[9];
        rawData[3] = (byte) 65;
        rawData[4] = (byte) 64;
        rawData[6] = (byte) 65;
        byte[] result = new AsciiHexInterpreter().uninterpret(rawData, 1, 3);
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) -48, result[0]);
    }

    @Test
    public void testUninterpret6() throws Throwable {
        byte[] rawData = new byte[6];
        rawData[1] = (byte) 65;
        byte[] result = new AsciiHexInterpreter().uninterpret(rawData, 1, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) -16, result[0]);
    }

    @Test
    public void testUninterpret7() throws Throwable {
        byte[] rawData = new byte[3];
        rawData[1] = (byte) 63;
        byte[] result = AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 15, result[0]);
    }

    @Test
    public void testUninterpret8() throws Throwable {
        byte[] rawData = new byte[1];
        byte[] result = new AsciiHexInterpreter().uninterpret(rawData, 100, 0);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] rawData = new byte[68];
        rawData[2] = (byte) 65;
        rawData[3] = (byte) 64;
        rawData[5] = (byte) 65;
        rawData[12] = (byte) 64;
        rawData[65] = (byte) 63;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "68", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] rawData = new byte[68];
        rawData[3] = (byte) 64;
        rawData[5] = (byte) 65;
        rawData[12] = (byte) 64;
        rawData[65] = (byte) 63;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "68", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] rawData = new byte[68];
        rawData[2] = (byte) 65;
        rawData[3] = (byte) 64;
        rawData[12] = (byte) 64;
        rawData[65] = (byte) 63;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "68", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] rawData = new byte[67];
        rawData[65] = (byte) 65;
        rawData[66] = (byte) 65;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 65, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "67", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] rawData = new byte[2];
        rawData[0] = (byte) 63;
        rawData[1] = (byte) 65;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] rawData = new byte[5];
        rawData[3] = (byte) 64;
        rawData[4] = (byte) 64;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 1, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "5", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException6() throws Throwable {
        byte[] rawData = new byte[1];
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException7() throws Throwable {
        byte[] rawData = new byte[3];
        rawData[0] = (byte) 66;
        try {
            new AsciiHexInterpreter().uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[1];
        try {
            new AsciiHexInterpreter().uninterpret(rawData, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
