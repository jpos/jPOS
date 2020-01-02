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
import java.io.Serializable;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Logger;

/**
 * The {@code SecurePrivateKey} class wraps any private key, which is protected
 * by the security module with variant methods.
 * <p>
 * The wrapped private key should be in the secure proprietary format of
 * the security module.
 * <p>The {@code keyType} indicates type of private key <i>(currently only RSA
 * keys are supported - others may be in future)</i>
 *
 * @author Robert Demski
 */
public class SecurePrivateKey extends SecureVariantKey implements Serializable{

    private static final long serialVersionUID = -9145281998779008306L;

    /**
     * Constructs an {@code SecurePrivateKey}.
     *
     * @param keyType eg. {@link SMAdapter#TYPE_RSA_PK} or {@link SMAdapter#TYPE_RSA_SK}
     * @param keyBytes private key in the secure proprietary format of the security module.
     */
    public SecurePrivateKey(String keyType, byte[] keyBytes) {
        setKeyType(keyType);
        setKeyBytes(keyBytes);
    }

    @Override
    public void setVariant(byte variant) {}

    @Override
    public byte getVariant() {
        throw new UnsupportedOperationException("Operation getVariant() not"
                + " allowed for " + SecurePrivateKey.class.getName());
    }

    @Override
    public void setScheme(KeyScheme scheme) {}

    @Override
    public KeyScheme getScheme() {
        throw new UnsupportedOperationException("Operation getScheme() not"
                + " allowed for " + SecurePrivateKey.class.getName());
    }

    /**
     * Dumps {@code SecureRSAPrivateKey} basic information.
     *
     * @param p a PrintStream usually supplied by {@link Logger}
     * @param indent indention string, usually suppiled by {@link Logger}
     */
    @Override
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<secure-rsa-private-key");
        p.print(" type=\"" + keyType + "\"");
        if (keyName != null)
            p.print(" name=\"" + keyName + "\"");

        p.println(">");
        p.println(inner + "<data>" + ISOUtil.hexString(getKeyBytes()) + "</data>");
        p.println(indent + "</secure-rsa-private-key>");
    }

}
