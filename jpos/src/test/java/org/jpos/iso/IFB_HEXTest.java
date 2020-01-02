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
 * @author apr (based on IFB_NUMERIC contributed by joconnor)
 */
public class IFB_HEXTest {
    @Test
    public void testPack() throws Exception {
        ISOField field = new ISOField(3, "0123456789ABCDEF");
        IFB_HEX packager = new IFB_HEX(16, "Should be 0123456789ABCDEF", true);
        TestUtils.assertEquals(
            new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF} ,
            packager.pack(field));
    }

    @Test
    public void testUnpack() throws Exception {
        byte[] raw = new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF}  ;
        IFB_HEX packager = new IFB_HEX(16, "Should be 0123456789ABCDEF", true);
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("0123456789ABCDEF", (String) field.getValue());
    }

    @Test
    public void testReversability() throws Exception {
        String origin = "0123456789ABCDEF";
        ISOField f = new ISOField(12, origin);
        IFB_HEX packager = new IFB_HEX(origin.length(), "Should be 0123456789ABCDEF", true);

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }

    @Test
    public void testLeftPad() throws Exception {
        String origin = "0123456789ABCDE";
        ISOField f = new ISOField(12, origin);
        IFB_HEX packager = new IFB_HEX(origin.length(), "Should be 00123456789ABCDE", true);

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals (origin, (String) unpack.getValue());
        assertEquals ("00123456789ABCDE", ISOUtil.hexString(packager.pack(f)));
    }

    @Test
    public void testRightPad() throws Exception {
        String origin = "0123456789ABCDE";
        ISOField f = new ISOField(12, origin);
        IFB_HEX packager = new IFB_HEX(origin.length(), "Should be 00123456789ABCDE", false);
        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals (origin, (String) unpack.getValue());
        assertEquals ("0123456789ABCDEF", ISOUtil.hexString(packager.pack(f)));
    }
}
