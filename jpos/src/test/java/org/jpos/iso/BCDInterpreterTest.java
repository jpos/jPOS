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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BCDInterpreterTest {

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = BCDInterpreter.LEFT_PADDED.getPackedLength(0);
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = BCDInterpreter.RIGHT_PADDED.getPackedLength(100);
        assertEquals("result", 50, result);
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[3];
        BCDInterpreter.RIGHT_PADDED_F.interpret(" ", b, 0);
        // changed in 1.6.8
        assertEquals("b[0]", (byte) 15, b[0]);

    }

    @Test
    public void testInterpret1() throws Throwable {
        byte[] b = new byte[4];
        BCDInterpreter.RIGHT_PADDED_F.interpret("", b, 100);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInterpret2() throws Throwable {
        byte[] b = new byte[3];
        b[2] = (byte) -87;
        BCDInterpreter.RIGHT_PADDED_F.interpret(" ", b, 0);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInterpret3() throws Throwable {
        byte[] b = new byte[2];
        BCDInterpreter.LEFT_PADDED.interpret("", b, 100);
        assertTrue("Test completed without Exception", true);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[1];
        BCDInterpreter.RIGHT_PADDED_F.interpret("testBCDInterpreterData", b, 100);
    }

    @Test(expected = NullPointerException.class)
    public void testInterpretThrowsNullPointerException() throws Throwable {
        BCDInterpreter.LEFT_PADDED.interpret("testBCDInterpreterData", (byte[]) null, 100);
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[3];
        String result = BCDInterpreter.LEFT_PADDED.uninterpret(rawData, 0, 1);
        assertEquals("result", "0", result);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] rawData = new byte[3];
        BCDInterpreter.RIGHT_PADDED_F.uninterpret(rawData, 100, 1000);
    }

    @Test(expected = NegativeArraySizeException.class)
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[0];
        BCDInterpreter.LEFT_PADDED.uninterpret(rawData, 100, -1);
    }

    @Test(expected = NullPointerException.class)
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        BCDInterpreter.LEFT_PADDED.uninterpret((byte[]) null, 100, 1000);
    }
}
