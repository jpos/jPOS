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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.ToIntFunction;
import org.jpos.iso.ISOUtil;

/**
 * The builder class to create and parse key block structure.
 */
public class SecureKeyBlockBuilder {

    /** Size in characters of the key-block version byte. */
    protected static final int SIZE_KEYBLOCK_VERSION    = 1;

    /** Size in characters of the total key-block length field. */
    protected static final int SIZE_KEYBLOCK_LENGTH     = 4;

    /** Size in characters of the key-usage attribute. */
    protected static final int SIZE_KEYUSAGE            = 2;

    /** Size in characters of the key-version field. */
    protected static final int SIZE_KEY_VERSION         = 2;

    /** Size in characters of the number-of-optional-headers field. */
    protected static final int SIZE_NUMOFOPTHDR         = 2;

    /** Size in characters of the reserved field. */
    protected static final int SIZE_RESERVED            = 2;

    /** Size in characters of the fixed key-block header. */
    protected static final int SIZE_HEADER              = 16;

    /** Size in characters of an optional header's identifier. */
    protected static final int SIZE_OPTHDR_ID           = 2;

    /** Size in characters of an optional header's length field. */
    protected static final int SIZE_OPTHDR_LENGTH       = 2;

    /** Size in characters of the MAC trailer when bound with 3-DES. */
    protected static final int SIZE_HEADER_3DES         = 8;

    /** Size in characters of the MAC trailer when bound with AES. */
    protected static final int SIZE_HEADER_AES          = 16;

    private final List<Character> versionsWith8CharacterMAC = new ArrayList<>(
        Arrays.asList(
             'A' // TR-31:2005 'A' Key block protected using the Key Variant Binding Method
            ,'B' // TR-31:2010 'B' Key block protected using the Key Derivation Binding Method
            ,'C' // TR-31:2010 'C' Key block protected using the Key Variant Binding Method
            ,'0' // Proprietary '0' Key block protected using the 3-DES key
        )
    );

    /**
     * Don't let anyone instantiate this class.
     */
    private SecureKeyBlockBuilder() {
    }

    /**
     * Returns a new {@code SecureKeyBlockBuilder} instance.
     *
     * @return a fresh builder
     */
    public static SecureKeyBlockBuilder newBuilder() {
        return new SecureKeyBlockBuilder();
    }

    /**
     * Configure key block versions with 8 digits key block MAC.
     * <p>
     * Default 8 digits <i>(4 bytes)</i> key block MAC versions are:
     * <ul>
     *   <li>'A' TR-31:2005 Key block protected using the Key Variant Binding Method
     *   <li>'B' TR-31:2010 Key block protected using the Key Derivation Binding Method
     *   <li>'C' TR-31:2010 Key block protected using the Key Variant Binding Method
     *   <li>'0' Proprietary Key block protected using the 3-DES key
     * </ul>
     * @param versions the string with versions characters
     * @return This builder instance
     */
    public SecureKeyBlockBuilder with8characterMACVersions(String versions) {
        Objects.requireNonNull(versions, "The versions with 8 digits MAC cannot be null");
        versionsWith8CharacterMAC.clear();
        for (Character ch : versions.toCharArray())
            versionsWith8CharacterMAC.add(ch);

        return this;
    }

    /**
     * Returns the MAC trailer length appropriate for the given key block's version.
     *
     * @param skb the key block being inspected
     * @return {@link #SIZE_HEADER_3DES} for 8-character MAC versions, {@link #SIZE_HEADER_AES} otherwise
     */
    protected int getMACLength(SecureKeyBlock skb) {
        if (versionsWith8CharacterMAC.contains(skb.getKeyBlockVersion()))
            return SIZE_HEADER_3DES;

        return SIZE_HEADER_AES;
    }


    /**
     * Reads {@code len} characters from {@code sr}.
     *
     * @param sr source reader
     * @param len number of characters to read
     * @return the characters read, as a {@code String}
     * @throws IllegalArgumentException if reading fails
     */
    protected static String readString(StringReader sr, int len) {
        char[] chars = new char[len];
        try {
            sr.read(chars);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Problem witch reading key block characters", ex);
        }
        return String.valueOf(chars);
    }

    /**
     * Reads a single character from {@code sr}.
     *
     * @param sr source reader
     * @return the character read
     * @throws IllegalArgumentException if reading fails
     */
    protected static char readChar(StringReader sr) {
        try {
            return (char) sr.read();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Problem witch reading key block character", ex);
        }
    }

    /**
     * Parses {@code numOfBlocks} optional header blocks from {@code sr} into a map.
     *
     * @param sr source reader, positioned at the first optional header
     * @param numOfBlocks number of optional headers to consume
     * @return a map from header identifier to header value, in iteration order
     */
    protected static Map<String, String> parseOptionalHeader(StringReader sr, int numOfBlocks) {
        Map<String, String> ret = new LinkedHashMap<>();
        int cnt = numOfBlocks;
        String hbi;
        int len;
        String hdata;
        while (cnt-- > 0) {
            hbi = readString(sr, SIZE_OPTHDR_ID);
            len = Integer.valueOf(readString(sr, SIZE_OPTHDR_LENGTH), 0x10);
            hdata = readString(sr, len - SIZE_OPTHDR_ID - SIZE_OPTHDR_LENGTH);
            ret.put(hbi, hdata);
        }
        return ret;
    }

    /**
     * Calculates the on-wire length contribution of the supplied optional headers.
     *
     * @param optHdrs optional headers
     * @return total characters required to encode them, including id and length prefixes
     */
    protected static int calcOptionalHeaderLength(Map<String, String> optHdrs) {
        ToIntFunction<String> entryLength = e -> {
                int l = SIZE_OPTHDR_ID + SIZE_OPTHDR_LENGTH;
                if (e != null)
                    l += e.length();

                return l;
        };
        Collection<String> c = optHdrs.values();
        return c.stream().mapToInt(entryLength).sum();
    }

    /**
     * Parses a key-block string into a populated {@link SecureKeyBlock}.
     *
     * @param data raw key-block characters (header, optional headers, encrypted key, MAC)
     * @return the parsed key block
     * @throws IllegalArgumentException if {@code data} is shorter than the fixed header or otherwise malformed
     */
    public SecureKeyBlock build(CharSequence data) throws IllegalArgumentException {
        Objects.requireNonNull(data, "The key block data cannot be null");
        SecureKeyBlock skb = new SecureKeyBlock();
        String keyblock = data.toString();
        if (keyblock.length() < SIZE_HEADER)
            throw new IllegalArgumentException("The key block data cannot be shorter than 16");

        StringReader sr = new StringReader(data.toString());
        skb.keyBlockVersion = readChar(sr);
        skb.keyBlockLength = Integer.valueOf(readString(sr, SIZE_KEYBLOCK_LENGTH));
        String ku = readString(sr, SIZE_KEYUSAGE);
        skb.keyUsage = ExtKeyUsage.valueOfByCode(ku);
        skb.algorithm = Algorithm.valueOfByCode(readChar(sr));
        skb.modeOfUse = ModeOfUse.valueOfByCode(readChar(sr));
        skb.keyVersion = readString(sr, SIZE_KEY_VERSION);
        skb.exportability = Exportability.valueOfByCode(readChar(sr));
        int numOfBlocks = Integer.valueOf(readString(sr, SIZE_NUMOFOPTHDR));
        skb.reserved = readString(sr, SIZE_RESERVED);
        skb.optionalHeaders = parseOptionalHeader(sr, numOfBlocks);
        int consumed = SIZE_HEADER + calcOptionalHeaderLength(skb.getOptionalHeaders());

        if (skb.getKeyBlockLength() <= consumed)
            // it can be but it should not occur
            return skb;

        int remain = skb.getKeyBlockLength() - consumed;
        int macLen = getMACLength(skb);
        String keyEnc = readString(sr, remain - macLen);
        if (!keyEnc.isEmpty())
            skb.setKeyBytes(ISOUtil.hex2byte(keyEnc));

        String mac = readString(sr, macLen);
        skb.keyBlockMAC = ISOUtil.hex2byte(mac);
        return skb;
    }

    /**
     * Serializes a {@link SecureKeyBlock} into its on-wire string form.
     *
     * @param skb the key block to serialize
     * @return the encoded key-block string (header + optional headers + encrypted key + MAC)
     */
    public String toKeyBlock(SecureKeyBlock skb) {
        StringBuilder sb = new StringBuilder();
        sb.append(skb.getKeyBlockVersion());
        sb.append(String.format("%04d", skb.getKeyBlockLength()));
        sb.append(skb.getKeyUsage().getCode());
        sb.append(skb.getAlgorithm().getCode());
        sb.append(skb.getModeOfUse().getCode());
        sb.append(skb.getKeyVersion());
        sb.append(skb.getExportability().getCode());

        Map<String, String> optHdr = skb.getOptionalHeaders();
        sb.append(String.format("%02d", optHdr.size()));
        sb.append(skb.getReserved());

        for (Entry<String, String> ent : optHdr.entrySet()) {
            sb.append(ent.getKey());
            sb.append(String.format("%02X", ent.getValue().length() + SIZE_OPTHDR_ID + SIZE_OPTHDR_LENGTH));
            sb.append(ent.getValue());
        }

        byte[] b = skb.getKeyBytes();
        if (b != null)
            sb.append(ISOUtil.hexString(b));

        b = skb.getKeyBlockMAC();
        if (b != null)
            sb.append(ISOUtil.hexString(skb.getKeyBlockMAC()));

        return sb.toString();
    }

}
