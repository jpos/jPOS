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

import org.jpos.util.Loggeable;

import java.io.Serializable;


/**
 * Represents a key that cannot be used except by your security module and for
 * performing the operations allowed by the security module for this type 
 * of keys.
 *
 * So, a SecureKey can be safely stored in a clear file or database.
 *
 * <p>
 * A SecureKey is typically a key encrypted under one of the secret keys that are
 * protected by the security module itself (Local Master Keys --LMK for short).<br>
 * </p>
 * <p>
 * SecureKey just holds:<br>
 * 1- Secure Key: a byte[] holding the key in the secure proprietary format
 *    of your security module. This is typically the clear key encrypted under LMK.<br>
 * 2- Key Type: identifies what this key can be used for (e.g. TYPE_ZPK
 *    (Zone PIN Key), TYPE_ZMK (Zone Master Key)...<BR>
 * 3- Key Length (in bits): also called key size. e.g. LENGTH_DES, LENGTH_DES3_2KEY,...etc.
 *    This is not necessarily deducible from the length of the byte[] holding
 *    the secure key bytes, since encryption under LMK is proprietary to the
 *    security module.
 * </p>
 * <p>
 * NOTE: The security of SecureKey is totally dependent on the security of
 * the used security module.
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 * @see SMAdapter SMAdapter
 *
 */
public abstract class SecureKey
        implements Serializable, Loggeable {
    /**
     * Secure Key Bytes
     */
    protected byte[] keyBytes = null;

    /**
     * The keyCheckValue allows identifying which clear key does this
     * secure key represent.
     */
    protected byte[] keyCheckValue;

    /**
     * This is the bit length of the key
     * This can be: LENGTH_DES, LENGTH_DES3_2KEY, ...
     */
    protected short keyLength;
    /**
     * Key Type is useful for stating what this key can be used for.
     * The value of Key Type specifies whether this encryped key is a
     * TYPE_TMK (Terminal Master Key), TYPE_ZPK (Zone PIN Key)....<BR>
     */
    protected String keyType;

    /**
     * Key scheme indicates protection metchod appiled to this key by
     * a security module.
     */
    protected KeyScheme scheme;

    /**
     * Optional key name
     */
    protected String keyName;

    /**
     * Sets the secure key bytes
     * @param keyBytes byte[] representing the secured key bytes
     */
    public void setKeyBytes (byte[] keyBytes) {
        this.keyBytes = keyBytes;
    }

    /**
     * @return The byte[] holding the secure key Bytes
     */
    public byte[] getKeyBytes () {
        return  keyBytes;
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
     * Sets the length of the key (in bits) (when it was still clear).
     * This might be different than the bit length of the secureKeyBytes.
     * @param keyLength
     */
    public void setKeyLength (short keyLength) {
        this.keyLength = keyLength;
    }

    /**
     * @return The Length of the secure key (when it was still clear)
     */
    public short getKeyLength () {
        return  keyLength;
    }

    /**
     * Key Type is useful for stating what this key can be used for.
     * The value of Key Type specifies whether this secure key is a
     * TYPE_TMK (Terminal Master Key), TYPE_ZPK (Zone PIN Key)....<BR>
     * @param keyType
     */
    public void setKeyType (String keyType) {
        this.keyType = keyType;
    }

    /**
     * Key Type is useful for stating what this key can be used for.
     * The value of Key Type specifies whether this secure key is a
     * TYPE_TMK (Terminal Master Key), TYPE_ZPK (Zone PIN Key)....<BR>
     * @return keyType
     */
    public String getKeyType () {
        return  this.keyType;
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
    public abstract KeyScheme getScheme();

    /**
     * optional key name
     */
    public String getKeyName() {
        return this.keyName;
    }
    /**
     * optional key name
     * @param keyName string
     */
    public void setKeyName (String keyName) {
        this.keyName = keyName;
    }
}

