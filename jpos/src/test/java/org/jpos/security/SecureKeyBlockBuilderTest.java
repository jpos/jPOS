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

package org.jpos.security;

import java.util.Map;
import java.util.Map.Entry;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class SecureKeyBlockBuilderTest {

    private static final byte[] KEYBLOCK_MAC8 = ISOUtil.hex2byte("A1B2C3D4");

    private static final byte[] KEYBLOCK_MAC4 = ISOUtil.hex2byte("E1F2");

    private static final byte[] KEYBLOCK_ENCKEY = ISOUtil.hex2byte("A9B8C7D6E5F49382");

    private static final String OPTHEADER_KS = "KS0ETest KS.12";

    private static final String OPTHEADER_KV = "KV08abcd";

    SecureKeyBlockBuilder instance;

    SecureKeyBlock ret;

    @BeforeEach
    public void setUp() {
        instance = SecureKeyBlockBuilder.newBuilder();
    }

    /**
     * Test of build method, of class SecureKeyBlock.
     */
    @Test
    public void testBuildNull() {
        assertThrows(NullPointerException.class, () -> {
            instance.build(null);
        });
    }

    /**
     * Test of build method, of class SecureKeyBlock.
     */
    @Test
    public void testBuildToShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            String data = "10024V2TG17N00";
            instance.build(data);
        });
    }

    /**
     * Test of build method, of class SecureKeyBlock.
     */
    @Test
    public void testBuildWithoutEncKey() {
        String data = "10024V2TG17N0033" + ISOUtil.hexString(KEYBLOCK_MAC8);
        ret = instance.build(data);

        assertEquals('1', ret.getKeyBlockVersion());
        assertEquals(24, ret.getKeyBlockLength());
        assertEquals(KeyUsage.VISAPVV, ret.getKeyUsage());
        assertEquals(Algorithm.TDES, ret.getAlgorithm());
        assertEquals(ModeOfUse.GENONLY, ret.getModeOfUse());
        assertEquals("17", ret.getKeyVersion());
        assertEquals(Exportability.NONE, ret.getExportability());
        assertEquals("33", ret.getReserved());
        assertNull(ret.getKeyBytes());
        assertArrayEquals(KEYBLOCK_MAC8, ret.getKeyBlockMAC());
    }

    /**
     * Test of build method, of class SecureKeyBlock.
     */
    @Test
    public void testBuildWithoutEncKeyAndMAC() {
        String data = "00016V2DN17N0033";
        ret = instance.build(data);

        assertEquals('0', ret.getKeyBlockVersion());
        assertEquals(16, ret.getKeyBlockLength());
        assertEquals(KeyUsage.VISAPVV, ret.getKeyUsage());
        assertEquals(Algorithm.DES, ret.getAlgorithm());
        assertEquals(ModeOfUse.ANY, ret.getModeOfUse());
        assertEquals("17", ret.getKeyVersion());
        assertEquals(Exportability.NONE, ret.getExportability());
        assertEquals("33", ret.getReserved());
        assertNull(ret.getKeyBytes());
        assertNull(ret.getKeyBlockMAC());
    }

    /**
     * Test of build method, of class SecureKeyBlock.
     */
    @Test
    public void testBuildWithoutWithoutEncKey() {
        String data = "00020P0ANc4S0021" + ISOUtil.hexString(KEYBLOCK_MAC4);
        ret = instance.build(data);

        assertEquals('0', ret.getKeyBlockVersion());
        assertEquals(20, ret.getKeyBlockLength());
        assertEquals(KeyUsage.PINENC, ret.getKeyUsage());
        assertEquals(Algorithm.AES, ret.getAlgorithm());
        assertEquals(ModeOfUse.ANY, ret.getModeOfUse());
        assertEquals("c4", ret.getKeyVersion());
        assertEquals(Exportability.TRUSTED, ret.getExportability());
        assertEquals("21", ret.getReserved());
        assertNull(ret.getKeyBytes());
        assertArrayEquals(KEYBLOCK_MAC4, ret.getKeyBlockMAC());
    }

    /**
     * Test of build method, of class SecureKeyBlock.
     */
    @Test
    public void testBuildMAC8() {
        String data = "00036V2RG17N0003" + ISOUtil.hexString(KEYBLOCK_ENCKEY) + ISOUtil.hexString(KEYBLOCK_MAC4);
        ret = instance.build(data);

        assertEquals('0', ret.getKeyBlockVersion());
        assertEquals(36, ret.getKeyBlockLength());
        assertEquals(KeyUsage.VISAPVV, ret.getKeyUsage());
        assertEquals(Algorithm.RSA, ret.getAlgorithm());
        assertEquals(ModeOfUse.GENONLY, ret.getModeOfUse());
        assertEquals("17", ret.getKeyVersion());
        assertEquals(Exportability.NONE, ret.getExportability());
        assertEquals("03", ret.getReserved());
        assertArrayEquals(KEYBLOCK_ENCKEY, ret.getKeyBytes());
        assertArrayEquals(KEYBLOCK_MAC4, ret.getKeyBlockMAC());
    }

    @Test
    public void testBuildWithOptHeaders() {
        String data = "D0046V2TGc3E0233" + OPTHEADER_KV + OPTHEADER_KS + ISOUtil.hexString(KEYBLOCK_MAC8);
        ret = instance.build(data);

        assertEquals('D', ret.getKeyBlockVersion());
        assertEquals(46, ret.getKeyBlockLength());

        assertNull(ret.getKeyBytes());
        assertArrayEquals(KEYBLOCK_MAC8, ret.getKeyBlockMAC());

        Map<String, String> ophdr = ret.getOptionalHeaders();
        assertEquals(2, ophdr.size());

        // Check optional headers content AND ORDER
        Entry<String, String>[] entries = ophdr.entrySet().toArray(new Entry[0]);
        assertEquals("KV", entries[0].getKey());
        assertEquals("abcd", entries[0].getValue());
        assertEquals("KS", entries[1].getKey());
        assertEquals("Test KS.12", entries[1].getValue());
    }

    @Test
    public void testBuildOptHeadersReversed() {
        String data = "E0046V2TGc3E0233" + OPTHEADER_KS + OPTHEADER_KV + ISOUtil.hexString(KEYBLOCK_MAC8);
        ret = instance.build(data);

        assertEquals('E', ret.getKeyBlockVersion());
        assertEquals(46, ret.getKeyBlockLength());

        assertNull(ret.getKeyBytes());
        assertArrayEquals(KEYBLOCK_MAC8, ret.getKeyBlockMAC());

        Map<String, String> ophdr = ret.getOptionalHeaders();
        assertEquals(2, ophdr.size());

        // Check optional headers content AND ORDER
        Entry<String, String>[] entries = ophdr.entrySet().toArray(new Entry[0]);
        assertEquals("KV", entries[1].getKey());
        assertEquals("abcd", entries[1].getValue());
        assertEquals("KS", entries[0].getKey());
        assertEquals("Test KS.12", entries[0].getValue());
    }

    /**
     * Test of toKeyBlock method, of class SecureKeyBlock.
     */
    @Test
    public void testToKeyBlock() {
        String data = "E0046V2TGc3E0233" + OPTHEADER_KS + OPTHEADER_KV + ISOUtil.hexString(KEYBLOCK_MAC8);
        ret = instance.build(data);

        assertEquals(data, instance.toKeyBlock(ret));
    }

    /**
     * Test of toKeyBlock method, of class SecureKeyBlock.
     */
    @Test
    public void testToKeyBlockReversed() {
        String data = "E0046V2TGc3E0233" + OPTHEADER_KV + OPTHEADER_KS + ISOUtil.hexString(KEYBLOCK_MAC8);
        ret = instance.build(data);

        assertEquals(data, instance.toKeyBlock(ret));
    }

}
