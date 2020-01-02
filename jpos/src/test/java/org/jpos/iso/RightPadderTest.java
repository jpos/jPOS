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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author joconnor
 */
public class RightPadderTest {
    private RightPadder padder;
    
    @BeforeEach
    public void setUp() {
        padder = new RightPadder('0');
    }
    
    @Test
    public void testPaddingNeeded() throws Exception {
        assertEquals("123000", padder.pad("123", 6));
    }

    @Test
    public void testNoPaddingNeeded() throws Exception {
        assertEquals("123", padder.pad("123", 3));
    }

    @Test
    public void testPadLengthTooShort() throws Exception {
        try {
            padder.pad("123", 2);
            fail("Padding a bigger string into a smaller buffer should throw an exception");
        } catch (Exception asIExpected) {
        }
    }

    @Test
    public void testUnpad1() throws Exception {
        assertEquals("123", padder.unpad("123000"));
    }

    @Test
    public void testUnpad2() throws Exception {
        assertEquals("123", padder.unpad("123"));
    }

    @Test
    public void testUnpad3() throws Exception {
        assertEquals("1203", padder.unpad("1203000"));
    }

    @Test
    public void testUnpadAllPadding() throws Exception {
        assertEquals("", padder.unpad("000"));
    }

    @Test
    public void testReversability() throws Exception {
        String origin = "Abc";
        assertEquals(origin, padder.unpad(padder.pad(origin, 6)));
    }
}
