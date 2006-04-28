/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
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
        p.println(">");
        p.println(inner + "<data>" + ISOUtil.hexString(getKeyBytes()) + "</data>");
        p.println(inner + "<check-value>" + ISOUtil.hexString(getKeyCheckValue()) + "</check-value>");
        p.println(indent + "</secure-des-key>");
    }
}



