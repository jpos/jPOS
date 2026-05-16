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

public class EMVICCPublicKeyTest {

    private static EMVICCPublicKey newKey() {
        return new EMVICCPublicKey(
                ISOUtil.hex2byte("12345678901234FFFFFF"),              // 10-byte PAN BCD
                ISOUtil.hex2byte("1231"),                              // expirationDate
                ISOUtil.hex2byte("ABCDEF"),                            // serialNumber
                ISOUtil.hex2byte("BCDEF012345678901122334455667788"), // modulus (16 bytes for test)
                ISOUtil.hex2byte("03"),                                // exponent
                (byte) 0x01,                                           // SHA-1
                (byte) 0x01);                                          // RSA
    }

    @Test
    public void testAccessorsReturnComponents() {
        byte[] pan      = ISOUtil.hex2byte("12345678901234FFFFFF");
        byte[] expDate  = ISOUtil.hex2byte("1231");
        byte[] serial   = ISOUtil.hex2byte("ABCDEF");
        byte[] modulus  = ISOUtil.hex2byte("BCDEF012345678901122334455667788");
        byte[] exponent = ISOUtil.hex2byte("03");
        EMVICCPublicKey k = new EMVICCPublicKey(
                pan, expDate, serial, modulus, exponent, (byte) 0x01, (byte) 0x01);
        assertArrayEquals(pan,      k.applicationPan());
        assertArrayEquals(expDate,  k.expirationDate());
        assertArrayEquals(serial,   k.serialNumber());
        assertArrayEquals(modulus,  k.modulus());
        assertArrayEquals(exponent, k.exponent());
        assertEquals((byte) 0x01, k.hashAlgorithmIndicator());
        assertEquals((byte) 0x01, k.publicKeyAlgorithmIndicator());
    }

    @Test
    public void testDefensiveCopiesArrayComponents() {
        byte[] pan      = ISOUtil.hex2byte("12345678901234FFFFFF");
        byte[] expDate  = ISOUtil.hex2byte("1231");
        byte[] serial   = ISOUtil.hex2byte("ABCDEF");
        byte[] modulus  = ISOUtil.hex2byte("BCDEF012345678901122334455667788");
        byte[] exponent = ISOUtil.hex2byte("03");
        EMVICCPublicKey k = new EMVICCPublicKey(
                pan, expDate, serial, modulus, exponent, (byte) 0x01, (byte) 0x01);
        pan[0] = 0x00;
        expDate[0] = 0x00;
        serial[0] = 0x00;
        modulus[0] = 0x00;
        exponent[0] = 0x00;
        k.applicationPan()[1] = 0x00;
        k.expirationDate()[1] = 0x00;
        k.serialNumber()[1] = 0x00;
        k.modulus()[1] = 0x00;
        k.exponent()[0] = 0x00;

        assertArrayEquals(ISOUtil.hex2byte("12345678901234FFFFFF"), k.applicationPan());
        assertArrayEquals(ISOUtil.hex2byte("1231"), k.expirationDate());
        assertArrayEquals(ISOUtil.hex2byte("ABCDEF"), k.serialNumber());
        assertArrayEquals(ISOUtil.hex2byte("BCDEF012345678901122334455667788"), k.modulus());
        assertArrayEquals(ISOUtil.hex2byte("03"), k.exponent());
    }

    @Test
    public void testEqualsContentBasedOnByteArrays() {
        EMVICCPublicKey a = newKey();
        EMVICCPublicKey b = newKey();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsFalseWhenModulusDiffersByOneByte() {
        EMVICCPublicKey a = newKey();
        EMVICCPublicKey b = new EMVICCPublicKey(
                ISOUtil.hex2byte("12345678901234FFFFFF"),
                ISOUtil.hex2byte("1231"),
                ISOUtil.hex2byte("ABCDEF"),
                ISOUtil.hex2byte("BCDEF012345678901122334455667789"), // last byte differs
                ISOUtil.hex2byte("03"),
                (byte) 0x01,
                (byte) 0x01);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseWhenPanDiffers() {
        EMVICCPublicKey a = newKey();
        EMVICCPublicKey b = new EMVICCPublicKey(
                ISOUtil.hex2byte("99999999999999FFFFFF"), // differs
                ISOUtil.hex2byte("1231"),
                ISOUtil.hex2byte("ABCDEF"),
                ISOUtil.hex2byte("BCDEF012345678901122334455667788"),
                ISOUtil.hex2byte("03"),
                (byte) 0x01,
                (byte) 0x01);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseForUnrelatedType() {
        EMVICCPublicKey a = newKey();
        assertFalse(a.equals("not-an-icc-key"));
        assertFalse(a.equals(null));
    }

    @Test
    public void testToStringRendersByteFieldsAsHex() {
        EMVICCPublicKey k = newKey();
        String s = k.toString();
        assertTrue(s.startsWith("EMVICCPublicKey["), "toString prefix: " + s);
        assertTrue(s.contains("12345678901234FFFFFF"), "toString should contain PAN hex: " + s);
        assertTrue(s.contains("1231"), "toString should contain expirationDate hex: " + s);
        assertTrue(s.contains("BCDEF012345678901122334455667788"),
                "toString should contain modulus hex: " + s);
    }

    @Test
    public void testToStringSurvivesNullByteFields() {
        EMVICCPublicKey k = new EMVICCPublicKey(null, null, null, null, null,
                (byte) 0x01, (byte) 0x01);
        assertTrue(k.toString().startsWith("EMVICCPublicKey["));
    }
}
