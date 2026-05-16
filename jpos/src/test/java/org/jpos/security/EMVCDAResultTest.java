/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

public class EMVCDAResultTest {

    private static EMVCDAResult newResult() {
        return new EMVCDAResult(ISOUtil.hex2byte("12345678"), (byte) 0x40);
    }

    @Test
    public void testAccessorsReturnComponents() {
        byte[] dyn = ISOUtil.hex2byte("12345678");
        EMVCDAResult r = new EMVCDAResult(dyn, (byte) 0x40);
        assertSame(dyn, r.iccDynamicNumber());
        assertEquals((byte) 0x40, r.cid());
    }

    @Test
    public void testEqualsContentBased() {
        EMVCDAResult a = newResult();
        EMVCDAResult b = newResult();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsFalseWhenDynamicNumberDiffers() {
        EMVCDAResult a = newResult();
        EMVCDAResult b = new EMVCDAResult(ISOUtil.hex2byte("12345679"), (byte) 0x40);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseWhenCidDiffers() {
        EMVCDAResult a = newResult();
        EMVCDAResult b = new EMVCDAResult(ISOUtil.hex2byte("12345678"), (byte) 0x80);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseForUnrelatedType() {
        EMVCDAResult a = newResult();
        assertFalse(a.equals("not-a-cda-result"));
        assertFalse(a.equals(null));
    }

    @Test
    public void testToStringRendersFieldsAsHex() {
        EMVCDAResult r = newResult();
        String s = r.toString();
        assertTrue(s.startsWith("EMVCDAResult["), "toString prefix: " + s);
        assertTrue(s.contains("12345678"), "toString should contain dynamic number hex: " + s);
        assertTrue(s.contains("cid=40"), "toString should render CID as hex: " + s);
    }
}
