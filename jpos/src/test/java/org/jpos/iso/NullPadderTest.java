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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class NullPadderTest {

    @Test
    public void testConstructor() throws Throwable {
        new NullPadder();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testPad() throws Throwable {
        String result = NullPadder.INSTANCE.pad("testNullPadderData", 100);
        assertEquals("testNullPadderData", result, "result");
    }

    @Test
    public void testPad1() throws Throwable {
        String result = new NullPadder().pad(null, 100);
        assertNull(result, "result");
    }

    @Test
    public void testUnpad() throws Throwable {
        String result = new NullPadder().unpad(null);
        assertNull(result, "result");
    }

    @Test
    public void testUnpad1() throws Throwable {
        String result = new NullPadder().unpad("testNullPadderPaddedData");
        assertEquals("testNullPadderPaddedData", result, "result");
    }
}
