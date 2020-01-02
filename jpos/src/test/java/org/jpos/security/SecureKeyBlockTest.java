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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class SecureKeyBlockTest {

    private static final String NL = System.getProperty("line.separator");

    private static final byte[] KEYBLOCK_MAC8 = ISOUtil.hex2byte("A1B2C3D4");

    private static final byte[] KEYBLOCK_MAC4 = ISOUtil.hex2byte("E1F2");

    private static final byte[] KEYBLOCK_ENCKEY = ISOUtil.hex2byte("A9B8C7D6E5F49382");

    private static final byte[] KEYBLOCK_KCV = ISOUtil.hex2byte("A1B2C3");


    PrintStream ps;
    ByteArrayOutputStream os;

    SecureKeyBlock instance;

    @BeforeEach
    public void setUp() {
        os = new ByteArrayOutputStream();
        ps = new PrintStream(os);
        instance = Mockito.spy(SecureKeyBlock.class);
    }

    /**
     * Test of setKeyCheckValue method, of class SecureKeyBlock.
     */
    @Test
    public void testSetKeyCheckValue() {
        byte[] keyCheckValue = null;
        instance.setKeyCheckValue(keyCheckValue);
    }

    /**
     * Test of getKeyCheckValue method, of class SecureKeyBlock.
     */
    @Test
    public void testGetKeyCheckValue() {
        byte[] expResult = null;
        byte[] result = instance.getKeyCheckValue();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of setKeyType method, of class SecureKeyBlock.
     */
    @Test
    public void testSetKeyType() {
        assertThrows(UnsupportedOperationException.class, () -> {
            instance.setKeyType("");
        });
    }

    /**
     * Test of getKeyType method, of class SecureKeyBlock.
     */
    @Test
    public void testGetKeyType() {
        assertThrows(UnsupportedOperationException.class, () -> {
            instance.getKeyType();
        });
    }

    /**
     * Test of setKeyLength method, of class SecureKeyBlock.
     */
    @Test
    public void testSetKeyLength() {
        assertThrows(UnsupportedOperationException.class, () -> {
            instance.setKeyLength((short) 128);
        });
    }

    /**
     * Test of getKeyLength method, of class SecureKeyBlock.
     */
    @Test
    public void testGetKeyLength() {
        assertThrows(UnsupportedOperationException.class, () -> {
            instance.getKeyLength();
        });
    }

    /**
     * Test of getScheme method, of class SecureKeyBlock.
     */
    @Test
    public void testGetScheme() {
        KeyScheme expResult = null;
        KeyScheme result = instance.getScheme();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOptionalHeaders method, of class SecureKeyBlock.
     */
    @Test
    public void testGetOptionalHeaders() {
        Map<String, String> expResult = new LinkedHashMap<>();
        Map<String, String> result = instance.getOptionalHeaders();
        assertEquals(expResult, result);
    }

    /**
     * Test of dump method, of class SecureKeyBlock.
     */
    @Test
    public void testDump() {
        String indent = "____";
        instance.setScheme(KeyScheme.R);
        instance.keyBlockVersion = 'D';
        instance.keyUsage = KeyUsage.CVK;
        instance.algorithm = Algorithm.TDES;
        instance.modeOfUse = ModeOfUse.ENCDEC;
        instance.keyVersion = "c2";
        instance.exportability = Exportability.ANY;
        instance.setKeyBytes(KEYBLOCK_ENCKEY);
        instance.keyBlockMAC = KEYBLOCK_MAC8;
        instance.setKeyCheckValue(KEYBLOCK_KCV);

        instance.dump(ps, indent);

        StringBuilder sb = new StringBuilder(128);
        sb.append(indent).append("<secure-key-block scheme=\"R\">").append(NL);
        sb.append(indent).append("  <header>").append(NL);
        sb.append(indent).append("    <version>D</version>").append(NL);
        sb.append(indent).append("    <key-usage>C0</key-usage>").append(NL);
        sb.append(indent).append("    <algorithm>T</algorithm>").append(NL);
        sb.append(indent).append("    <mode-of-use>B</mode-of-use>").append(NL);
        sb.append(indent).append("    <key-version>c2</key-version>").append(NL);
        sb.append(indent).append("    <exportability>E</exportability>").append(NL);
        sb.append(indent).append("  </header>").append(NL);
        sb.append(indent).append("  <data>").append(ISOUtil.hexString(KEYBLOCK_ENCKEY)).append("</data>").append(NL);
        sb.append(indent).append("  <mac>").append(ISOUtil.hexString(KEYBLOCK_MAC8)).append("</mac>").append(NL);
        sb.append(indent).append("  <check-value>").append(ISOUtil.hexString(KEYBLOCK_KCV)).append("</check-value>").append(NL);
        sb.append(indent).append("</secure-key-block>").append(NL);

        assertEquals(sb.toString(), os.toString());
    }

    /**
     * Test of dump method, of class SecureKeyBlock.
     */
    @Test
    public void testDumpWithNameAndOptHeader() {
        String indent = "____";
        instance.setScheme(KeyScheme.S);
        instance.keyBlockVersion = 'D';
        instance.keyUsage = KeyUsage.CVK;
        instance.algorithm = Algorithm.AES;
        instance.modeOfUse = ModeOfUse.ENCDEC;
        instance.keyVersion = "c2";
        instance.exportability = Exportability.ANY;
        instance.reserved = "19";
        instance.setKeyBytes(KEYBLOCK_ENCKEY);
        instance.keyBlockMAC = KEYBLOCK_MAC8;
        instance.setKeyCheckValue(KEYBLOCK_KCV);
        instance.setKeyName("Test key block key");
        LinkedHashMap<String, String> optHdr = new LinkedHashMap<>();
        instance.optionalHeaders = optHdr;
        optHdr.put("KS", "Test KS.12");
        optHdr.put("KV", "abcd");

        instance.dump(ps, indent);

        StringBuilder sb = new StringBuilder(128);
        sb.append(indent).append("<secure-key-block scheme=\"S\" name=\"Test key block key\">").append(NL);
        sb.append(indent).append("  <header>").append(NL);
        sb.append(indent).append("    <version>D</version>").append(NL);
        sb.append(indent).append("    <key-usage>C0</key-usage>").append(NL);
        sb.append(indent).append("    <algorithm>A</algorithm>").append(NL);
        sb.append(indent).append("    <mode-of-use>B</mode-of-use>").append(NL);
        sb.append(indent).append("    <key-version>c2</key-version>").append(NL);
        sb.append(indent).append("    <exportability>E</exportability>").append(NL);
        sb.append(indent).append("    <reserved>19</reserved>").append(NL);
        sb.append(indent).append("  </header>").append(NL);
        sb.append(indent).append("  <optional-header>").append(NL);
        sb.append(indent).append("    <entry id=\"KS\" value=\"Test KS.12\"/>").append(NL);
        sb.append(indent).append("    <entry id=\"KV\" value=\"abcd\"/>").append(NL);
        sb.append(indent).append("  </optional-header>").append(NL);
        sb.append(indent).append("  <data>").append(ISOUtil.hexString(KEYBLOCK_ENCKEY)).append("</data>").append(NL);
        sb.append(indent).append("  <mac>").append(ISOUtil.hexString(KEYBLOCK_MAC8)).append("</mac>").append(NL);
        sb.append(indent).append("  <check-value>").append(ISOUtil.hexString(KEYBLOCK_KCV)).append("</check-value>").append(NL);
        sb.append(indent).append("</secure-key-block>").append(NL);

        assertEquals(sb.toString(), os.toString());
    }

}
