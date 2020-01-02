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
import java.util.Map;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class SecureKeySpecTest {

    private static final String NL = System.getProperty("line.separator");

    private static final byte[] KEYBLOCK_MAC8 = ISOUtil.hex2byte("A1B2C3D4");

    private static final byte[] KEYBLOCK_ENCKEY = ISOUtil.hex2byte("A9B8C7D6E5F49382");

    private static final byte[] KCV_KEYBLOCK = ISOUtil.hex2byte("A1B2C3");

    private static final byte[] KCV_VARIANT  = ISOUtil.hex2byte("F9E8D7");


    PrintStream ps;
    ByteArrayOutputStream os;

    SecureKeySpec instance;

    @BeforeEach
    public void setUp() {
        os = new ByteArrayOutputStream();
        ps = new PrintStream(os);

        instance = new SecureKeySpec();
    }

    /**
     * Test of dump method, of class SecureKeySpec.
     */
    @Test
    public void testDumpEmpty() {
        String indent = "____";

        instance.dump(ps, indent);

        StringBuilder sb = new StringBuilder(128);
        sb.append(indent).append("<secure-key-spec>").append(NL);
        sb.append(indent).append("</secure-key-spec>").append(NL);

        assertEquals(sb.toString(), os.toString());
    }

    /**
     * Test of dump method, of class SecureKeySpec.
     */
    @Test
    public void testDumpVariant() {
        String indent = "____";
        instance.setScheme(KeyScheme.U);
        instance.setKeyLength(SMAdapter.LENGTH_DES3_2KEY);
        instance.setKeyType(SMAdapter.TYPE_CVK);
        instance.setVariant(3);
        instance.setKeyCheckValue(KCV_VARIANT);
        instance.setKeyName("Test variant key");

        instance.dump(ps, indent);

        StringBuilder sb = new StringBuilder(128);
        sb.append(indent).append("<secure-key-spec scheme=\"U\" name=\"Test variant key\">").append(NL);
        sb.append(indent).append("  <length>128</length>").append(NL);
        sb.append(indent).append("  <type>CVK</type>").append(NL);
        sb.append(indent).append("  <variant>3</variant>").append(NL);
        sb.append(indent).append("  <check-value>").append(ISOUtil.hexString(KCV_VARIANT)).append("</check-value>").append(NL);
        sb.append(indent).append("</secure-key-spec>").append(NL);

        assertEquals(sb.toString(), os.toString());
    }

    /**
     * Test of dump method, of class SecureKeySpec.
     */
    @Test
    public void testDumpKeyBlock() {
        String indent = "____";
        instance.setScheme(KeyScheme.S);
        instance.setKeyBlockVersion('1');
        instance.setKeyUsage(KeyUsage.KEK);
        instance.setAlgorithm(Algorithm.DSA);
        instance.setModeOfUse(ModeOfUse.GENONLY);
        instance.setKeyVersion("39");
        instance.setExportability(Exportability.NONE);
        instance.setReserved("04");
        instance.setKeyBytes(KEYBLOCK_ENCKEY);
        instance.setKeyBlockMAC(KEYBLOCK_MAC8);
        instance.setKeyCheckValue(KCV_KEYBLOCK);
        instance.setKeyName("Test key block key");

        instance.dump(ps, indent);

        StringBuilder sb = new StringBuilder(128);
        sb.append(indent).append("<secure-key-spec scheme=\"S\" name=\"Test key block key\">").append(NL);
        sb.append(indent).append("  <header>").append(NL);
        sb.append(indent).append("    <version>1</version>").append(NL);
        sb.append(indent).append("    <key-usage>K0</key-usage>").append(NL);
        sb.append(indent).append("    <algorithm>S</algorithm>").append(NL);
        sb.append(indent).append("    <mode-of-use>G</mode-of-use>").append(NL);
        sb.append(indent).append("    <key-version>39</key-version>").append(NL);
        sb.append(indent).append("    <exportability>N</exportability>").append(NL);
        sb.append(indent).append("    <reserved>04</reserved>").append(NL);
        sb.append(indent).append("  </header>").append(NL);
        sb.append(indent).append("  <data>").append(ISOUtil.hexString(KEYBLOCK_ENCKEY)).append("</data>").append(NL);
        sb.append(indent).append("  <mac>").append(ISOUtil.hexString(KEYBLOCK_MAC8)).append("</mac>").append(NL);
        sb.append(indent).append("  <check-value>").append(ISOUtil.hexString(KCV_KEYBLOCK)).append("</check-value>").append(NL);
        sb.append(indent).append("</secure-key-spec>").append(NL);

        assertEquals(sb.toString(), os.toString());
    }

    /**
     * Test of dump method, of class SecureKeySpec.
     */
    @Test
    public void testDumpKeyBlockWithOptionalHeader() {
        String indent = "____";
        instance.setScheme(KeyScheme.R);
        instance.setKeyBlockVersion('D');
        instance.setKeyUsage(KeyUsage.CVK);
        instance.setModeOfUse(ModeOfUse.GENVER);
        Map optHdr = instance.getOptionalHeaders();
        optHdr.put("KS", "Test KS.12");
        optHdr.put("KV", "abcd");
        optHdr.put("99", "quick brown fox");

        instance.dump(ps, indent);

        StringBuilder sb = new StringBuilder(128);
        sb.append(indent).append("<secure-key-spec scheme=\"R\">").append(NL);
        sb.append(indent).append("  <header>").append(NL);
        sb.append(indent).append("    <version>D</version>").append(NL);
        sb.append(indent).append("    <key-usage>C0</key-usage>").append(NL);
        sb.append(indent).append("    <mode-of-use>C</mode-of-use>").append(NL);
        sb.append(indent).append("  </header>").append(NL);
        sb.append(indent).append("  <optional-header>").append(NL);
        sb.append(indent).append("    <entry id=\"KS\" value=\"Test KS.12\"/>").append(NL);
        sb.append(indent).append("    <entry id=\"KV\" value=\"abcd\"/>").append(NL);
        sb.append(indent).append("    <entry id=\"99\" value=\"quick brown fox\"/>").append(NL);
        sb.append(indent).append("  </optional-header>").append(NL);
        sb.append(indent).append("</secure-key-spec>").append(NL);

        assertEquals(sb.toString(), os.toString());
    }

}
