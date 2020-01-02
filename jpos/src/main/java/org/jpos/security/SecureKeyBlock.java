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

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jpos.iso.ISOUtil;

/**
 * The class represents a secure key in key block form (TR-31 or derivatives).
 * <p>
 * In addition to standard Key Chcek Value and Key Schema, specifies the key
 * block header, optional key block header, encrypted key and key block MAC.
 * <p>
 * The {@code SecureKeyBlock} instance can come from HSM <i>(generate, import,
 * translate)</i> or from the key store. And this is an integral whole.
 * Therefore, manipulation of key block values is not desirable. This is the
 * reason why the key block setters methods are not available. Use the
 * {@code SecureKeyBlockBuilder} to create the key block structure.
 */
public class SecureKeyBlock extends SecureKey {

    /**
     * Identifies the method by which the key block is cryptographically
     * protected and the content layout of the block.
     */
    protected char keyBlockVersion = ' ';

    /**
     * Entire key block length after encoding (header, optional header,
     * encrypted confidential data, and MAC).
     */
    protected int keyBlockLength;

    /**
     * The primary usage of the key contained in the key block.
     */
    protected KeyUsage keyUsage;

    /**
     * The cryptographic algorithm with which the key contained in key block
     * will be used.
     */
    protected Algorithm algorithm = Algorithm.TDES;

    /**
     * The operation that the key contained in the key block can perform.
     */
    protected ModeOfUse modeOfUse = ModeOfUse.ANY;

    /**
     * Version number to optionally indicate that the contents of the key block
     * is a component (key part), or to prevent re-injection of an old key.
     */
    protected String keyVersion = "  ";

    /**
     * The conditions under which the key can be exported outside the
     * cryptographic domain.
     */
    protected Exportability exportability = Exportability.ANY;

    /**
     * This element is not specified by TR-31 (should contain two ASCII zeros).
     * <p>
     * In proprietary derivatives can be used as e.g: LMK identifier.
     */
    protected String reserved = "00";

    /**
     * The TR-31 Key Block format allows a key block to contain up to 99
     * Optional Header Blocks which can be used to include additional (optional)
     * data within the Key Block.
     */
    protected Map<String, String> optionalHeaders = new LinkedHashMap<>();

    /**
     * The key block MAC ensures the integrity of the key block, and is
     * calculated over the Header, Optional Header Blocks and the encrypted Key
     * Data.
     */
    protected byte[] keyBlockMAC;

    /**
     * Constructs an SecureKeyBlock.
     * <p>
     * It can be used internally by e.g: {@code SecureKeyBlockBuilder}.
     */
    protected SecureKeyBlock() {
        super();
    }

    @Override
    public void setKeyType(String keyType) {
        throw new UnsupportedOperationException(
            "Operation setKeyType() not allowed for " + SecureKeyBlock.class.getName()
        );
    }

    @Override
    public String getKeyType() {
        throw new UnsupportedOperationException(
            "Operation getKeyType() not allowed for " + SecureKeyBlock.class.getName()
        );
    }

    @Override
    public void setKeyLength(short keyLength) {
        throw new UnsupportedOperationException(
            "Operation setKeyLength() not allowed for " + SecureKeyBlock.class.getName()
        );
    }

    @Override
    public short getKeyLength() {
        throw new UnsupportedOperationException(
            "Operation getKeyLength() not allowed for " + SecureKeyBlock.class.getName()
        );
    }

    @Override
    public KeyScheme getScheme() {
        return scheme;
    }

    /**
     * Identifies the method by which the key block is cryptographically
     * protected and the content layout of the block.
     *
     * @return The key block version that corresponds to byte 0 of the key block.
     */
    public char getKeyBlockVersion() {
        return keyBlockVersion;
    }

    /**
     * Entire key block length after encoding (header, optional header,
     * encrypted confidential data, and MAC).
     *
     * @return The key block length that corresponds to bytes 1-4 of the key block.
     */
    public int getKeyBlockLength() {
        return keyBlockLength;
    }

    /**
     * The primary usage of the key contained in the key block.
     *
     * @return The key usage that corresponds to bytes 5-6 of the key block.
     */
    public KeyUsage getKeyUsage() {
        return keyUsage;
    }

    /**
     * The cryptographic algorithm with which the key contained in key block
     * will be used.
     *
     * @return The key algorithm that corresponds to byte 7 of the key block.
     */
    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * The operation that the key contained in the key block can perform.
     *
     * @return The mode of use that corresponds to byte 8 of the key block.
     */
    public ModeOfUse getModeOfUse() {
        return modeOfUse;
    }

    /**
     * Version number to optionally indicate that the contents of the key block
     * is a component (key part), or to prevent re-injection of an old key.
     *
     * @return The key version that corresponds to bytes 9-10 of the key block.
     */
    public String getKeyVersion() {
        return keyVersion;
    }

    /**
     * The conditions under which the key can be exported outside the
     * cryptographic domain.
     *
     * @return The key exportability that corresponds to byte 11 of the key block.
     */
    public Exportability getExportability() {
        return exportability;
    }

    /**
     * This element is not specified by TR-31 (should contain two ASCII zeros).
     * <p>
     * In proprietary derivatives can be used as e.g: LMK identifier.
     *
     * @return The reserved that corresponds to bytes 14-15 of the key block.
     */
    public String getReserved() {
        return reserved;
    }

    /**
     * The key blok Optional Header Blocks.
     * <p>
     * The number of optional heders corresponds to bytes 12-13 of the key block.
     * <p>
     * The order of the elements in the map is preserved by {@code LinkedHashMap}
     *
     * @return Read only map of Optional Key Blok Heders.
     */
    public Map<String, String> getOptionalHeaders() {
        return Collections.unmodifiableMap(optionalHeaders);
    }

    /**
     * The key block MAC ensures the integrity of the key block.
     * <p>
     * It is calculated over the Header, Optional Header Blocks and the
     * encrypted Key Data.
     * The length of the MAC depends on the type of LMK key:
     * <ul>
     *   <li>4 bytes for DES Key Block LMK
     *   <li>8 bytes for AES Key Block LMK
     * </ul>
     *
     * @return calculated key block MAC value.
     */
    public byte[] getKeyBlockMAC() {
        return keyBlockMAC;
    }

    /**
     * Dumps SecureKeyBlock basic information
     *
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    @Override
    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        String inner2 = inner + "  ";
        p.print(indent + "<secure-key-block");
        p.print(" scheme=\"" + scheme + "\"");
        if (keyName != null)
            p.print(" name=\"" + keyName + "\"");

        p.println(">");

        p.println(inner + "<header>");
        p.println(inner2 + "<version>" + keyBlockVersion + "</version>");
        p.println(inner2 + "<key-usage>" + keyUsage.getCode() + "</key-usage>");
        p.println(inner2 + "<algorithm>" + algorithm.getCode() + "</algorithm>");
        p.println(inner2 + "<mode-of-use>" + modeOfUse.getCode() + "</mode-of-use>");
        p.println(inner2 + "<key-version>" + keyVersion + "</key-version>");
        p.println(inner2 + "<exportability>" + exportability.getCode() + "</exportability>");
        if (reserved != null && !"00".equals(reserved))
            p.println(inner2 + "<reserved>" + reserved + "</reserved>");
        p.println(inner + "</header>");

        if (!optionalHeaders.isEmpty()) {
            p.println(inner + "<optional-header>");
            for (Entry<String, String> ent : optionalHeaders.entrySet())
                p.println(inner2 + "<entry id=\""+ ent.getKey() + "\" value=\""+ ent.getValue()+ "\"/>");
            p.println(inner + "</optional-header>");
        }

        p.println(inner + "<data>" + ISOUtil.hexString(getKeyBytes()) + "</data>");
        p.println(inner + "<mac>" + ISOUtil.hexString(getKeyBlockMAC()) + "</mac>");
        p.println(inner + "<check-value>" + ISOUtil.hexString(getKeyCheckValue()) + "</check-value>");
        p.println(indent + "</secure-key-block>");
    }

}
