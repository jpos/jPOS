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

import org.jpos.iso.ISOUtil;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SecureDESKey class represents: <br>
 * Single, double or triple length DES keys that are secured by a security module.
 * This is typically the DES key encrypted under one of the Local Master Keys of the
 * security module.
 * <p>
 * SecureDESKey has an extra property "Key Check Value". It allows assuring that
 * two SecureDESKeys owned by two different parties map
 * to the same clear key. This can be a useful manual check for successful key
 * exchange.
 * <p>
 * NOTE: The security of SecureDESKey is totally dependent on the security of
 * the used security module.
 *
 * @author Hani S. Kirollos
 * @author Robert Demski
 * @see SMAdapter
 */
public class SecureDESKey extends SecureVariantKey {

    private static final long serialVersionUID = -9145281998779008306L;

    /**
     * Regular expression pattern representing key type string value.
     */
    protected static final Pattern KEY_TYPE_PATTERN = Pattern.compile("([^:;]*)([:;])?([^:;])?([^:;])?");

    public SecureDESKey() {
        super();
    }

    /**
     * Constructs an SecureDESKey
     * @param keyLength e.g. LENGTH_DES, LENGTH_DES3_2KEY, LENGTH_DES3_3KEY
     * @param keyType
     * @param variant
     * @param scheme
     * @param keyBytes DES Key in the secure proprietary format of your security module
     * @param keyCheckValue
     * @see SMAdapter
     */
    public SecureDESKey (short keyLength, String keyType, byte variant, KeyScheme scheme, byte[] keyBytes,
            byte[] keyCheckValue) {
        setKeyLength(keyLength);
        setKeyType(keyType);
        setVariant(variant);
        setScheme(scheme);
        setKeyBytes(keyBytes);
        setKeyCheckValue(keyCheckValue);
    }

    /**
     * Constructs an SecureDESKey
     * @param keyLength e.g. LENGTH_DES, LENGTH_DES3_2KEY, LENGTH_DES3_3KEY
     * @param keyType
     * @param keyBytes DES Key in the secure proprietary format of your security module
     * @param keyCheckValue
     * @see SMAdapter
     */
    public SecureDESKey (short keyLength, String keyType, byte[] keyBytes,
            byte[] keyCheckValue) {
        setKeyLength(keyLength);
        setKeyType(keyType);
        setKeyBytes(keyBytes);
        setKeyCheckValue(keyCheckValue);
        getVariant(); //only for set variant with defaults
        getScheme();  //only set scheme with defaults
    }

    /**
     * Constructs an SecureDESKey
     * @param keyLength
     * @param keyType
     * @param keyHexString secure key represented as HexString instead of byte[]
     * @param keyCheckValueHexString key check value represented as HexString instead of byte[]
     */
    public SecureDESKey (short keyLength, String keyType, String keyHexString,
            String keyCheckValueHexString) {
        this(keyLength, keyType, ISOUtil.hex2byte(keyHexString), ISOUtil.hex2byte(keyCheckValueHexString));
    }

    /**
     * Constructs an SecureDESKey
     * @param keyLength
     * @param keyType
     * @param variant
     * @param scheme
     * @param keyHexString secure key represented as HexString instead of byte[]
     * @param keyCheckValueHexString key check value represented as HexString instead of byte[]
     */
    public SecureDESKey (short keyLength, String keyType, byte variant, KeyScheme scheme, String keyHexString,
            String keyCheckValueHexString) {
        this(keyLength, keyType, variant, scheme, ISOUtil.hex2byte(keyHexString), ISOUtil.hex2byte(keyCheckValueHexString));
    }

    @Override
    public byte getVariant () {
        if (variant!=null)
            return variant;
        /**
         * Some variant derivation if it hasn't been explicity stated
         */
        variant = 0;
        Matcher m = KEY_TYPE_PATTERN.matcher(keyType);
        m.find();
        if (m.group(3) != null)
            try {
                variant = Byte.valueOf(m.group(3));
            } catch (NumberFormatException ex){
                throw new NumberFormatException("Value "+m.group(4)+" is not valid key variant");
            }
        return variant;
    }

    @Override
    public KeyScheme getScheme () {
        if (scheme!=null)
            return scheme;
        /**
         * Some scheme derivation if it hasn't been explicity stated
         */
        switch (keyLength){
            case SMAdapter.LENGTH_DES:
                scheme = KeyScheme.Z; break;
            case SMAdapter.LENGTH_DES3_2KEY:
                scheme = KeyScheme.X; break;
            case SMAdapter.LENGTH_DES3_3KEY:
                scheme = KeyScheme.Y; break;
        }
        Matcher m = KEY_TYPE_PATTERN.matcher(keyType);
        m.find();
        if (m.group(4) != null)
            try {
                scheme = KeyScheme.valueOf(m.group(4));
            } catch (IllegalArgumentException ex){
                throw new IllegalArgumentException("Value "+m.group(4)+" is not valid key scheme");
            }
        return scheme;
    }

    /**
     * dumps SecureDESKey basic information
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    @Override
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<secure-des-key");
        p.print(" length=\"" + getKeyLength() + "\"");
        p.print(" type=\"" + keyType + "\"");
        p.print(" variant=\"" + getVariant() + "\"");
        p.print(" scheme=\"" + this.getScheme() + "\"");
        if (keyName != null)
            p.print(" name=\"" + keyName + "\"");

        p.println(">");
        p.println(inner + "<data>" + ISOUtil.hexString(getKeyBytes()) + "</data>");
        p.println(inner + "<check-value>" + ISOUtil.hexString(getKeyCheckValue()) + "</check-value>");
        p.println(indent + "</secure-des-key>");
    }
}



