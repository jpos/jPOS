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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

public class EMVCAPublicKeyTest {

    private static EMVCAPublicKey newKey() {
        return new EMVCAPublicKey(
                ISOUtil.hex2byte("A000000003"),                      // rid (Visa)
                (byte) 0x07,                                          // index
                ISOUtil.hex2byte("BCDEF012345678901122334455667788"), // modulus (16 bytes for test)
                ISOUtil.hex2byte("03"),                               // exponent
                (byte) 0x01,                                          // SHA-1
                (byte) 0x01);                                         // RSA
    }

    @Test
    public void testAccessorsReturnComponents() {
        byte[] rid = ISOUtil.hex2byte("A000000003");
        byte[] modulus = ISOUtil.hex2byte("BCDEF012345678901122334455667788");
        byte[] exponent = ISOUtil.hex2byte("03");
        EMVCAPublicKey k = new EMVCAPublicKey(rid, (byte) 0x07, modulus, exponent,
                (byte) 0x01, (byte) 0x01);
        assertArrayEquals(rid, k.rid());
        assertEquals((byte) 0x07, k.index());
        assertArrayEquals(modulus, k.modulus());
        assertArrayEquals(exponent, k.exponent());
        assertEquals((byte) 0x01, k.hashAlgorithmIndicator());
        assertEquals((byte) 0x01, k.publicKeyAlgorithmIndicator());
    }

    @Test
    public void testDefensiveCopiesArrayComponents() {
        byte[] rid = ISOUtil.hex2byte("A000000003");
        byte[] modulus = ISOUtil.hex2byte("BCDEF012345678901122334455667788");
        byte[] exponent = ISOUtil.hex2byte("03");
        EMVCAPublicKey k = new EMVCAPublicKey(rid, (byte) 0x07, modulus, exponent,
                (byte) 0x01, (byte) 0x01);
        rid[0] = 0x00;
        modulus[0] = 0x00;
        exponent[0] = 0x00;
        k.rid()[1] = 0x00;
        k.modulus()[1] = 0x00;
        k.exponent()[0] = 0x00;

        assertArrayEquals(ISOUtil.hex2byte("A000000003"), k.rid());
        assertArrayEquals(ISOUtil.hex2byte("BCDEF012345678901122334455667788"), k.modulus());
        assertArrayEquals(ISOUtil.hex2byte("03"), k.exponent());
    }

    @Test
    public void testEqualsContentBasedOnByteArrays() {
        EMVCAPublicKey a = newKey();
        EMVCAPublicKey b = newKey();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsFalseWhenModulusDiffersByOneByte() {
        EMVCAPublicKey a = newKey();
        EMVCAPublicKey b = new EMVCAPublicKey(
                ISOUtil.hex2byte("A000000003"),
                (byte) 0x07,
                ISOUtil.hex2byte("BCDEF012345678901122334455667789"), // last byte differs
                ISOUtil.hex2byte("03"),
                (byte) 0x01,
                (byte) 0x01);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseWhenIndexDiffers() {
        EMVCAPublicKey a = newKey();
        EMVCAPublicKey b = new EMVCAPublicKey(
                ISOUtil.hex2byte("A000000003"),
                (byte) 0x08, // index differs
                ISOUtil.hex2byte("BCDEF012345678901122334455667788"),
                ISOUtil.hex2byte("03"),
                (byte) 0x01,
                (byte) 0x01);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseForUnrelatedType() {
        EMVCAPublicKey a = newKey();
        assertFalse(a.equals("not-a-ca-key"));
        assertFalse(a.equals(null));
    }

    @Test
    public void testToStringRendersByteFieldsAsHex() {
        EMVCAPublicKey k = newKey();
        String s = k.toString();
        assertTrue(s.startsWith("EMVCAPublicKey["), "toString prefix: " + s);
        assertTrue(s.contains("A000000003"), "toString should contain RID hex: " + s);
        assertTrue(s.contains("BCDEF012345678901122334455667788"),
                "toString should contain modulus hex: " + s);
        assertTrue(s.contains("index=07"), "toString should render index as hex: " + s);
    }

    @Test
    public void testToStringSurvivesNullByteFields() {
        EMVCAPublicKey k = new EMVCAPublicKey(null, (byte) 0x07, null, null,
                (byte) 0x01, (byte) 0x01);
        // Should not throw NPE
        assertTrue(k.toString().startsWith("EMVCAPublicKey["));
    }
}
