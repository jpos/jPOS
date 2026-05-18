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

public class EMVIssuerPublicKeyTest {

    private static EMVIssuerPublicKey newKey() {
        return new EMVIssuerPublicKey(
                ISOUtil.hex2byte("1234FFFF"),                          // issuerIdentifier
                ISOUtil.hex2byte("1231"),                              // expirationDate (Dec 2031)
                ISOUtil.hex2byte("ABCDEF"),                            // serialNumber
                ISOUtil.hex2byte("BCDEF012345678901122334455667788"), // modulus (16 bytes for test)
                ISOUtil.hex2byte("03"),                                // exponent
                (byte) 0x01,                                           // SHA-1
                (byte) 0x01);                                          // RSA
    }

    @Test
    public void testAccessorsReturnComponents() {
        byte[] issuerId = ISOUtil.hex2byte("1234FFFF");
        byte[] expDate  = ISOUtil.hex2byte("1231");
        byte[] serial   = ISOUtil.hex2byte("ABCDEF");
        byte[] modulus  = ISOUtil.hex2byte("BCDEF012345678901122334455667788");
        byte[] exponent = ISOUtil.hex2byte("03");
        EMVIssuerPublicKey k = new EMVIssuerPublicKey(
                issuerId, expDate, serial, modulus, exponent, (byte) 0x01, (byte) 0x01);
        assertArrayEquals(issuerId, k.issuerIdentifier());
        assertArrayEquals(expDate,  k.expirationDate());
        assertArrayEquals(serial,   k.serialNumber());
        assertArrayEquals(modulus,  k.modulus());
        assertArrayEquals(exponent, k.exponent());
        assertEquals((byte) 0x01, k.hashAlgorithmIndicator());
        assertEquals((byte) 0x01, k.publicKeyAlgorithmIndicator());
    }

    @Test
    public void testDefensiveCopiesArrayComponents() {
        byte[] issuerId = ISOUtil.hex2byte("1234FFFF");
        byte[] expDate  = ISOUtil.hex2byte("1231");
        byte[] serial   = ISOUtil.hex2byte("ABCDEF");
        byte[] modulus  = ISOUtil.hex2byte("BCDEF012345678901122334455667788");
        byte[] exponent = ISOUtil.hex2byte("03");
        EMVIssuerPublicKey k = new EMVIssuerPublicKey(
                issuerId, expDate, serial, modulus, exponent, (byte) 0x01, (byte) 0x01);
        issuerId[0] = 0x00;
        expDate[0] = 0x00;
        serial[0] = 0x00;
        modulus[0] = 0x00;
        exponent[0] = 0x00;
        k.issuerIdentifier()[1] = 0x00;
        k.expirationDate()[1] = 0x00;
        k.serialNumber()[1] = 0x00;
        k.modulus()[1] = 0x00;
        k.exponent()[0] = 0x00;

        assertArrayEquals(ISOUtil.hex2byte("1234FFFF"), k.issuerIdentifier());
        assertArrayEquals(ISOUtil.hex2byte("1231"), k.expirationDate());
        assertArrayEquals(ISOUtil.hex2byte("ABCDEF"), k.serialNumber());
        assertArrayEquals(ISOUtil.hex2byte("BCDEF012345678901122334455667788"), k.modulus());
        assertArrayEquals(ISOUtil.hex2byte("03"), k.exponent());
    }

    @Test
    public void testEqualsContentBasedOnByteArrays() {
        EMVIssuerPublicKey a = newKey();
        EMVIssuerPublicKey b = newKey();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsFalseWhenModulusDiffersByOneByte() {
        EMVIssuerPublicKey a = newKey();
        EMVIssuerPublicKey b = new EMVIssuerPublicKey(
                ISOUtil.hex2byte("1234FFFF"),
                ISOUtil.hex2byte("1231"),
                ISOUtil.hex2byte("ABCDEF"),
                ISOUtil.hex2byte("BCDEF012345678901122334455667789"), // last byte differs
                ISOUtil.hex2byte("03"),
                (byte) 0x01,
                (byte) 0x01);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseWhenExpirationDateDiffers() {
        EMVIssuerPublicKey a = newKey();
        EMVIssuerPublicKey b = new EMVIssuerPublicKey(
                ISOUtil.hex2byte("1234FFFF"),
                ISOUtil.hex2byte("0125"), // differs
                ISOUtil.hex2byte("ABCDEF"),
                ISOUtil.hex2byte("BCDEF012345678901122334455667788"),
                ISOUtil.hex2byte("03"),
                (byte) 0x01,
                (byte) 0x01);
        assertNotEquals(a, b);
    }

    @Test
    public void testEqualsFalseForUnrelatedType() {
        EMVIssuerPublicKey a = newKey();
        assertFalse(a.equals("not-an-issuer-key"));
        assertFalse(a.equals(null));
    }

    @Test
    public void testToStringRendersByteFieldsAsHex() {
        EMVIssuerPublicKey k = newKey();
        String s = k.toString();
        assertTrue(s.startsWith("EMVIssuerPublicKey["), "toString prefix: " + s);
        assertTrue(s.contains("1234FFFF"), "toString should contain issuerIdentifier hex: " + s);
        assertTrue(s.contains("1231"), "toString should contain expirationDate hex: " + s);
        assertTrue(s.contains("ABCDEF"), "toString should contain serial hex: " + s);
        assertTrue(s.contains("BCDEF012345678901122334455667788"),
                "toString should contain modulus hex: " + s);
    }

    @Test
    public void testToStringSurvivesNullByteFields() {
        EMVIssuerPublicKey k = new EMVIssuerPublicKey(null, null, null, null, null,
                (byte) 0x01, (byte) 0x01);
        assertTrue(k.toString().startsWith("EMVIssuerPublicKey["));
    }
}
