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

public class EMVDerivedKeyTest {

    private static SecureDESKey newKey() {
        return new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
                ,SMAdapter.TYPE_MK_AC+":1U"
                ,"0D39A43C864D1B40F33998B80BB02C95", "6FB1C8");
    }

    @Test
    public void testAccessorsReturnComponents() {
        SecureDESKey key = newKey();
        byte[] kcv = ISOUtil.hex2byte("ABCDEF");
        EMVDerivedKey<SecureDESKey> d = new EMVDerivedKey<>(key, kcv);
        assertSame(key, d.key());
        assertSame(kcv, d.kcv());
    }

    @Test
    public void testEqualsContentBasedOnKcv() {
        SecureDESKey key = newKey();
        EMVDerivedKey<SecureDESKey> a = new EMVDerivedKey<>(key, ISOUtil.hex2byte("ABCDEF"));
        EMVDerivedKey<SecureDESKey> b = new EMVDerivedKey<>(key, ISOUtil.hex2byte("ABCDEF"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsReflexive() {
        EMVDerivedKey<SecureDESKey> d = new EMVDerivedKey<>(newKey(), ISOUtil.hex2byte("ABCDEF"));
        assertEquals(d, d);
    }

    @Test
    public void testEqualsFalseWhenKcvDiffersByOneByte() {
        SecureDESKey key = newKey();
        EMVDerivedKey<SecureDESKey> a = new EMVDerivedKey<>(key, ISOUtil.hex2byte("ABCDEF"));
        EMVDerivedKey<SecureDESKey> b = new EMVDerivedKey<>(key, ISOUtil.hex2byte("ABCDEE"));
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseWhenKeyDiffers() {
        SecureDESKey k1 = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
                ,SMAdapter.TYPE_MK_AC+":1U"
                ,"0D39A43C864D1B40F33998B80BB02C95", "6FB1C8");
        SecureDESKey k2 = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
                ,SMAdapter.TYPE_MK_SMI+":2U"
                ,"E86D8A2FC81DEC4E91F9FE76EDAF3C3B", "6FB1C8");
        byte[] kcv = ISOUtil.hex2byte("ABCDEF");
        EMVDerivedKey<SecureDESKey> a = new EMVDerivedKey<>(k1, kcv);
        EMVDerivedKey<SecureDESKey> b = new EMVDerivedKey<>(k2, kcv);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseForUnrelatedType() {
        EMVDerivedKey<SecureDESKey> d = new EMVDerivedKey<>(newKey(), ISOUtil.hex2byte("ABCDEF"));
        assertFalse(d.equals("not-a-derived-key"));
        assertFalse(d.equals(null));
    }

    @Test
    public void testToStringRendersKcvAsHex() {
        EMVDerivedKey<SecureDESKey> d = new EMVDerivedKey<>(newKey(), ISOUtil.hex2byte("ABCDEF"));
        String s = d.toString();
        assertTrue(s.contains("ABCDEF"), "toString should contain hex KCV, got: " + s);
        assertTrue(s.startsWith("EMVDerivedKey["), "toString prefix mismatch: " + s);
    }

    @Test
    public void testToStringSurvivesNullKcv() {
        EMVDerivedKey<SecureDESKey> d = new EMVDerivedKey<>(newKey(), null);
        // Should not throw NPE
        assertTrue(d.toString().startsWith("EMVDerivedKey["));
    }
}
