/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

import java.io.PrintStream;

import org.jpos.iso.ISOUtil;

/**
 * <p>
 * The SecureDESKey class represents: <br>
 * Single, double or triple length DES keys that are secured by a security module.
 * This is typically the DES key encrypted under one of the Local Master Keys of the
 * security module.
 * </p>
 * <p>
 * SecureDESKey has an extra property "Key Check Value". It allows assuring that
 * two SecureDESKeys owned by two different parties map
 * to the same clear key. This can be a useful manual check for successful key
 * exchange.
 * </p>
 * <p>
 * NOTE: The security of SecureDESKey is totally dependent on the security of
 * the used security module.
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 * @see SMAdapter
 */
public class SecureDESKey extends SecureKey {

    private static final long serialVersionUID = -9145281998779008306L;
    /**
     * The keyCheckValue allows identifying which clear key does this
     * secure key represent.<br>
     */
    protected byte[] keyCheckValue = null;

    public SecureDESKey() {
        super();
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
     * The Key Check Value is typically a 24-bits (3 bytes) formed by encrypting a
     * block of zeros under the secure key when the secure key is clear
     * (not in this class, but inside the security module).
     * This check value allows identifying if two secure keys map to the
     * same clear key.
     * @param keyCheckValue
     */
    public void setKeyCheckValue (byte[] keyCheckValue) {
        this.keyCheckValue = keyCheckValue;
    }

    /**
     * The Key Check Value is typically a 24-bits (3 bytes) formed by encrypting a
     * block of zeros under the secure key when the secure key is clear
     * (not in this class, but inside the security module).
     * @return the keyCheckValue that was set before by setKeyCheckValue()
     */
    public byte[] getKeyCheckValue () {
        return  keyCheckValue;
    }

    /**
     * dumps SecureDESKey basic information
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<secure-des-key");
        p.print(" length=\"" + getKeyLength() + "\"");
        p.print(" type=\"" + keyType + "\"");
        if (keyName != null)
            p.print(" name=\"" + keyName + "\"");

        p.println(">");
        p.println(inner + "<data>" + ISOUtil.hexString(getKeyBytes()) + "</data>");
        p.println(inner + "<check-value>" + ISOUtil.hexString(getKeyCheckValue()) + "</check-value>");
        p.println(indent + "</secure-des-key>");
    }
}



