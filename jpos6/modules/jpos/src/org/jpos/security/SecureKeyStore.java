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

import org.jpos.iso.ISOException;


/**
 * <p>
 * Represents a collection of Secure Keys and typically stores them in some
 * persistent storage. SecureKeyStore isolates from particular DB implementations.
 * A Secure Key Store need not implement any security itself, it just holds keys
 * that are inherently secure (like SecureDESKey).
 * </p>
 * <p>
 * Note: SecureKeyStore doesn't have any relation with java.security.KeyStore
 * SecureKeyStore works on objects of type org.jpos.security.SecureKey
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 * @see SecureKey
 */
public interface SecureKeyStore {

    public static class SecureKeyStoreException extends ISOException {

        public SecureKeyStoreException () {
            super();
        }

        public SecureKeyStoreException (String detail) {
            super(detail);
        }

        public SecureKeyStoreException (Exception nested) {
            super(nested);
        }

        public SecureKeyStoreException (String detail, Exception nested) {
            super(detail, nested);
        }
    }



    /**
     * returns the key assiciated with the given alias
     * @param alias the alias name
     * @return the requested key, or null if the given alias does not exist.
     * @throws SecureKeyStoreException if SecureKeyStore is not initialized or if
     * the operation fails for some other reason.
     */
    public SecureKey getKey (String alias) throws SecureKeyStoreException;



    /**
     * Assigns the given key to the given alias.
     * If the given alias already exists, the keystore information associated
     * with it is overridden by the given key.
     * @param alias the alias name
     * @param key the key to be associated with the alias
     * @throws SecureKeyStoreException if SecureKeyStore is not initialized or the key
     * can't be recovered.
     */
    public void setKey (String alias, SecureKey key) throws SecureKeyStoreException;
}



