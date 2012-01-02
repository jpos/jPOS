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

import junit.framework.TestCase;

/**
 * @author joconnor
 */
public class RightPadderTest extends TestCase {
    private RightPadder padder;
    
    protected void setUp() {
        padder = new RightPadder('0');
    }
    
    public void testPaddingNeeded() throws Exception {
        assertEquals("123000", padder.pad("123", 6));
    }

    public void testNoPaddingNeeded() throws Exception {
        assertEquals("123", padder.pad("123", 3));
    }

    public void testPadLengthTooShort() throws Exception {
        try {
            padder.pad("123", 2);
            fail("Padding a bigger string into a smaller buffer should throw an exception");
        } catch (Exception asIExpected) {
        }
    }

    public void testUnpad1() throws Exception {
        assertEquals("123", padder.unpad("123000"));
    }

    public void testUnpad2() throws Exception {
        assertEquals("123", padder.unpad("123"));
    }

    public void testUnpad3() throws Exception {
        assertEquals("1203", padder.unpad("1203000"));
    }

    public void testUnpadAllPadding() throws Exception {
        assertEquals("", padder.unpad("000"));
    }

    public void testReversability() throws Exception {
        String origin = "Abc";
        assertEquals(origin, padder.unpad(padder.pad(origin, 6)));
    }
}
