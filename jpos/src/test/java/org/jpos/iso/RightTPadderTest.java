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
import static org.junit.Assert.fail;

import org.junit.Test;

public class RightTPadderTest {

    @Test
    public void testPad() throws Throwable {
        String result = new RightTPadder(' ').pad("10Characte", 10);
        assertEquals("result", "10Characte", result);
    }

    @Test
    public void testPad1() throws Throwable {
        String result = RightTPadder.SPACE_PADDER.pad("", 100);
        assertEquals("result",
                "                                                                                                    ", result);
    }

    @Test
    public void testPad2() throws Throwable {
        String result = new RightTPadder(' ').pad("testRightTPadderData", 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testPadThrowsNullPointerException() throws Throwable {
        try {
            new RightTPadder(' ').pad(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testPadThrowsStringIndexOutOfBoundsException() throws Throwable {
        try {
            new RightTPadder(' ').pad("testRightTPadderData", -1);
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: -1", ex.getMessage());
        }
    }
}
