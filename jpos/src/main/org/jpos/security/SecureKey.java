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

import java.io.Serializable;

import org.jpos.util.Loggeable;


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
}



