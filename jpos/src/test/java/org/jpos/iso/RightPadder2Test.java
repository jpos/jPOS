/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import static org.junit.Assert.fail;

import org.junit.Test;

public class RightPadder2Test {

    @Test
    public void testPad() throws Throwable {
        String result = new RightPadder(' ').pad("", 100);
        assertEquals("result",
                "                                                                                                    ", result);
    }

    @Test
    public void testPad1() throws Throwable {
        String result = new RightPadder(' ').pad("", 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testPadThrowsISOException() throws Throwable {
        try {
            new RightPadder(' ').pad("testRightPadderData", 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "Data is too long. Max = 0", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testPadThrowsNullPointerException() throws Throwable {
        try {
            new RightPadder(' ').pad(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpad() throws Throwable {
        String result = RightPadder.SPACE_PADDER.unpad(" ");
        assertEquals("result", "", result);
    }

    @Test
    public void testUnpad1() throws Throwable {
        String result = RightTPadder.SPACE_PADDER.unpad("X ");
        assertEquals("result", "X", result);
    }

    @Test
    public void testUnpad2() throws Throwable {
        String result = new RightPadder(' ').unpad("1");
        assertEquals("result", "1", result);
    }

    @Test
    public void testUnpad3() throws Throwable {
        String result = new RightPadder(' ').unpad("");
        assertEquals("result", "", result);
    }

    @Test
    public void testUnpadThrowsNullPointerException() throws Throwable {
        try {
            new RightPadder(' ').unpad(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
