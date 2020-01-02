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

import org.junit.jupiter.api.Test;

/**
 * @author joconnor
 * @author Mark Salter
 */
public class IFEPE_LLCHARTest {
    @Test
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(92, "123");
        IFEPE_LLCHAR packager = new IFEPE_LLCHAR(92, "Should be 0592123");
        TestUtils.assertEquals(new byte[] {(byte)0xF9, (byte)0xF2,(byte)0xf0, (byte)0xF3,  (byte)0xF1, (byte)0xF2, (byte)0xF3},
                            packager.pack(field));
    }

    @Test
    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {(byte)0xF9, (byte)0xF2,(byte)0xF0, (byte)0xF3,  (byte)0xF1, (byte)0xF2, (byte)0xF3};
        IFEPE_LLCHAR packager = new IFEPE_LLCHAR(10, "Should be 0592123");
        ISOField field = new ISOField();
        int len = packager.unpack(field, raw, 0);
        assertEquals(raw.length, len);
        String v = (String)field.getValue();
        assertEquals("123", (String) field.getValue());
        assertEquals(92, field.fieldNumber);    // Derived from TAG!
    }

    @Test
    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(92, origin);
        IFEPE_LLCHAR packager = new IFEPE_LLCHAR(92, "Should be Abc123:.-");

        ISOField unpack = new ISOField();
        int len = packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin.length()+2+2, len);
        assertEquals(origin, (String) unpack.getValue());
        assertEquals(92,unpack.fieldNumber);    // Derived from TAG!
    }
    
    @Test
    public void testReversability2() throws Exception
    {
        String origin = "P";
        ISOField f = new ISOField(87, origin);
        IFEPE_LLCHAR packager = new IFEPE_LLCHAR(87, "Should be P");

        ISOField unpack = new ISOField();
        byte[] packed = packager.pack(f);
        int len = packager.unpack(unpack, packed , 0);
        assertEquals(origin.length()+2+2, len);
        assertEquals(origin, unpack.getValue());
        assertEquals(87,unpack.fieldNumber);    // Derived from TAG!
    }
}
