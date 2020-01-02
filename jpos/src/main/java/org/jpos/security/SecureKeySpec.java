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

package  org.jpos.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.jpos.util.Loggeable;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jpos.iso.ISOUtil;

/**
 * This class contains a set of desirable key properties that can be passed to
 * an HSM device for example to generate a key or import it.
 * <p>
 * This class is not intended to use for key storage. It can contain
 * confidentional data like key length. That is why they should not be kept
 * persistently anywhere.
 *
 * @author Robert Demski
 */
public class SecureKeySpec
        implements Serializable, Loggeable {

    private static final long serialVersionUID = -3145281298749096305L;

    /**
     * Key scheme indicates protection metchod appiled to this key by
     * a security module.
     */
    protected KeyScheme scheme;

    /**
     * The key length is expressed in bits and refers to clear key <i>(before
     * LMK protection)</i>.
     */
    protected int keyLength;

    /**
     * Key Type is useful for stating what this key can be used for.
     * <p>
     * The value of Key Type specifies whether this encryped key is a
     * <ul>
     *   <li>{@link SMAdapter#TYPE_TMK} Terminal Master Key
     *   <li>{@link SMAdapter#TYPE_ZPK} Zone PIN Key
     *   <li>or others
     * </ul>
     */
    protected String keyType;

    /**
     * Indicates key protection variant metchod appiled to this key by a security module.
     */
    protected int variant;

    /**
     * Secure Key Bytes.
     */
    protected byte[] keyBytes = null;

    /**
     * The keyCheckValue allows identifying which clear key does this
     * secure key represent.
     */
    protected byte[] keyCheckValue;

    /**
     * Identifies the method by which the key block is cryptographically
     * protected and the content layout of the block.
     */
    protected char keyBlockVersion;

    /**
     * The primary usage of the key contained in the key block.
     */
    protected KeyUsage keyUsage;

    /**
     * The cryptographic algorithm with which the key contained in key block
     * will be used.
     */
    protected Algorithm algorithm;

    /**
     * The operation that the key contained in the key block can perform.
     */
    protected ModeOfUse modeOfUse;

    /**
     * Version number to optionally indicate that the contents of the key block
     * is a component (key part), or to prevent re-injection of an old key.
     */
    protected String keyVersion;

    /**
     * The conditions under which the key can be exported outside the
     * cryptographic domain.
     */
    protected Exportability exportability;

    /**
     * This element is not specified by TR-31 (should contain two ASCII zeros).
     * <p>
     * In proprietary derivatives can be used as e.g: LMK identifier.
     */
    protected String reserved;

    /**
     * The TR-31 Key Block format allows a key block to contain up to 99
     * Optional Header Blocks which can be used to include additional (optional)
     * data within the Key Block.
     */
    protected final Map<String, String> optionalHeaders = new LinkedHashMap<>();

    /**
     * The key block MAC ensures the integrity of the key block, and is
     * calculated over the Header, Optional Header Blocks and the encrypted Key
     * Data.
     */
    protected byte[] keyBlockMAC;

    /**
     * Optional key name.
     */
    protected String keyName;

    public SecureKeySpec() {
        super();
    }

    /**
     * Key scheme indicates protection metchod appiled to this key by
     * the security module.
     *
     * @param scheme key scheme used to protect this key.
     */
    public void setScheme(KeyScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the key scheme used to protect this key.
     *
     * @return key scheme used to protect this key.
     */
    public KeyScheme getScheme() {
        return scheme;
    }

    /**
     * Sets the length of the key.
     * <p>
     * The key length is expressed in bits and refers to clear key <i>(before
     * LMK protection)</i>
     * This might be different than the bit length of the secureKeyBytes.
     *
     * @param keyLength
     */
    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    /**
     * Gets the length of the key.
     * <p>
     * The key length is expressed in bits and refers to clear key <i>(before
     * LMK protection)</i>
     *
     * @return The length of the clear key
     */
    public int getKeyLength() {
        return keyLength;
    }

    /**
     * Key Type is useful for stating what this key can be used for.
     * <p>
     * The value of Key Type specifies whether this secure key is a
     * <ul>
     *   <li>{@link SMAdapter#TYPE_TMK} Terminal Master Key
     *   <li>{@link SMAdapter#TYPE_ZPK} Zone PIN Key
     *   <li>or others
     * </ul>
     *
     * @param keyType type of the key
     */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * Key Type is useful for stating what this key can be used for.
     * <p>
     * The value of Key Type specifies whether this secure key is a
     * <ul>
     *   <li>{@link SMAdapter#TYPE_TMK} Terminal Master Key
     *   <li>{@link SMAdapter#TYPE_ZPK} Zone PIN Key
     *   <li>or others
     * </ul>
     *
     * @return keyType type of the key
     */
    public String getKeyType() {
        return this.keyType;
    }

    /**
     * Sets key protection variant metchod appiled to this key by the security module.
     *
     * @param variant key variant method used to protect this key.
     */
    public void setVariant(int variant) {
        this.variant = variant;
    }

    /**
     * Gets the key variant method used to protect this key.
     *
     * @return key variant method used to protect this key.
     */
    public int getVariant() {
        return this.variant;
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

    public void setKeyBlockVersion(char keyBlockVersion) {
        this.keyBlockVersion = keyBlockVersion;
    }

    /**
     * The primary usage of the key contained in the key block.
     *
     * @return The key usage that corresponds to bytes 5-6 of the key block.
     */
    public KeyUsage getKeyUsage() {
        return keyUsage;
    }

    public void setKeyUsage(KeyUsage keyUsage) {
        this.keyUsage = keyUsage;
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

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * The operation that the key contained in the key block can perform.
     *
     * @return The mode of use that corresponds to byte 8 of the key block.
     */
    public ModeOfUse getModeOfUse() {
        return modeOfUse;
    }

    public void setModeOfUse(ModeOfUse modeOfUse) {
        this.modeOfUse = modeOfUse;
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

    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
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

    public void setExportability(Exportability exportability) {
        this.exportability = exportability;
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

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    /**
     * The key blok Optional Header Blocks.
     * <p>
     * The number of optional heders corresponds to bytes 12-13 of the key block.
     * <p>
     * The order of the elements in the map is preserved by {@code LinkedHashMap}
     *
     * @return map of Optional Key Blok Heders.
     */
    public Map<String, String> getOptionalHeaders() {
        return optionalHeaders;
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

    public void setKeyBlockMAC(byte[] keyBlockMAC) {
        this.keyBlockMAC = keyBlockMAC;
    }

    /**
     * Sets the secure key bytes.
     *
     * @param keyBytes bytes representing the secured key
     */
    public void setKeyBytes(byte[] keyBytes) {
        this.keyBytes = keyBytes;
    }

    /**
     * @return The bytes representing the secured key
     */
    public byte[] getKeyBytes() {
        return keyBytes;
    }

    /**
     * The Key Check Value is typically a 24-bits (3 bytes) formed by encrypting a
     * block of zeros under the secure key when the secure key is clear.
     * <p>
     * This check value allows identifying if two secure keys map to the
     * same clear key.
     *
     * @param keyCheckValue the Key Check Value
     */
    public void setKeyCheckValue(byte[] keyCheckValue) {
        this.keyCheckValue = keyCheckValue;
    }

    /**
     * The Key Check Value is typically a 24-bits (3 bytes) formed by encrypting
     * a block of zeros under the secure key when the secure key is clear.
     *
     * @return the Key Check Value
     */
    public byte[] getKeyCheckValue() {
        return keyCheckValue;
    }

    /**
     * Gets optional key name.
     *
     * @return name of the key
     */
    public String getKeyName() {
        return this.keyName;
    }

    /**
     * Sets optional key name.
     *
     * @param keyName name of the key
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * Dumps SecureKeySpec information.
     *
     * @param p a print stream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    @Override
    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<secure-key-spec");

        if (scheme != null)
            p.print(" scheme=\"" + scheme + "\"");

        if (keyName != null)
            p.print(" name=\"" + keyName + "\"");

        p.println(">");

        if (getKeyLength() > 0)
            p.println(inner + "<length>" + getKeyLength() + "</length>");

        if (getKeyType() != null) {
            p.println(inner + "<type>" + getKeyType() + "</type>");
            p.println(inner + "<variant>" + getVariant() + "</variant>");
        }

        String keyblok = formKeyHeader(inner);
        if (keyblok != null) {
            p.println(inner + "<header>");
            p.print(keyblok);
            p.println(inner + "</header>");
        }

        if (!optionalHeaders.isEmpty()) {
            p.println(inner + "<optional-header>");
            String inner2 = inner + "  ";
            for (Entry<String, String> ent : optionalHeaders.entrySet())
                p.println(inner2 + "<entry id=\""+ ent.getKey() + "\" value=\""+ ent.getValue()+ "\"/>");
            p.println(inner + "</optional-header>");
        }

        if (getKeyBytes() != null)
            p.println(inner + "<data>" + ISOUtil.hexString(getKeyBytes()) + "</data>");

        if (getKeyBlockMAC() != null)
            p.println(inner + "<mac>" + ISOUtil.hexString(getKeyBlockMAC()) + "</mac>");

        if (getKeyCheckValue() != null)
            p.println(inner + "<check-value>" + ISOUtil.hexString(getKeyCheckValue()) + "</check-value>");

        p.println(indent + "</secure-key-spec>");
    }

    protected String formKeyHeader(String indent) {
        String inner = indent + "  ";
        try (
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream p = new PrintStream(os);
        ) {
            if (keyBlockVersion != 0)
                p.println(inner + "<version>" + keyBlockVersion + "</version>");

            if (keyUsage != null)
                p.println(inner + "<key-usage>" + keyUsage.getCode() + "</key-usage>");

            if (algorithm != null)
                p.println(inner + "<algorithm>" + algorithm.getCode() + "</algorithm>");

            if (modeOfUse != null)
                p.println(inner + "<mode-of-use>" + modeOfUse.getCode() + "</mode-of-use>");

            if (keyVersion != null)
                p.println(inner + "<key-version>" + keyVersion + "</key-version>");

            if (exportability != null)
                p.println(inner + "<exportability>" + exportability.getCode() + "</exportability>");

            if (reserved != null)
                p.println(inner + "<reserved>" + reserved + "</reserved>");

            String ret = os.toString();
            if (ret.isEmpty())
                return null;

            return ret;
        } catch (IOException ex) {
            // for close(), it should never happens
            return null;
        }
    }

}

