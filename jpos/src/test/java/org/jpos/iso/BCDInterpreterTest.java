/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BCDInterpreterTest {

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = BCDInterpreter.LEFT_PADDED.getPackedLength(0);
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = BCDInterpreter.RIGHT_PADDED.getPackedLength(100);
        assertEquals(50, result, "result");
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[3];
        BCDInterpreter.RIGHT_PADDED_F.interpret(" ", b, 0);
        // changed in 1.6.8
        assertEquals((byte) 15, b[0], "b[0]");

    }

    @Test
    public void testInterpret1() throws Throwable {
        byte[] b = new byte[4];
        BCDInterpreter.RIGHT_PADDED_F.interpret("", b, 100);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testInterpret2() throws Throwable {
        byte[] b = new byte[3];
        b[2] = (byte) -87;
        BCDInterpreter.RIGHT_PADDED_F.interpret(" ", b, 0);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testInterpret3() throws Throwable {
        byte[] b = new byte[2];
        BCDInterpreter.LEFT_PADDED.interpret("", b, 100);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] b = new byte[1];
            BCDInterpreter.RIGHT_PADDED_F.interpret("testBCDInterpreterData", b, 100);
        });
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            BCDInterpreter.LEFT_PADDED.interpret("testBCDInterpreterData", null, 100);
        });
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[3];
        String result = BCDInterpreter.LEFT_PADDED.uninterpret(rawData, 0, 1);
        assertEquals("0", result, "result");
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] rawData = new byte[3];
            BCDInterpreter.RIGHT_PADDED_F.uninterpret(rawData, 100, 1000);
        });
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(NegativeArraySizeException.class, () -> {
            byte[] rawData = new byte[0];
            BCDInterpreter.LEFT_PADDED.uninterpret(rawData, 100, -1);
        });
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            BCDInterpreter.LEFT_PADDED.uninterpret(null, 100, 1000);
        });
    }
}
