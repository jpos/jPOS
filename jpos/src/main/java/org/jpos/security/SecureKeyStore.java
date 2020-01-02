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

import org.jpos.iso.ISOException;

import java.util.Map;


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

    class SecureKeyStoreException extends ISOException {

        private static final long serialVersionUID = 1976885367352075834L;

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
     * Returns the key assiciated with the given alias.
     *
     * @param <T> desired type of requested key
     * @param alias the alias name
     * @return the requested key, or {@code null} if the given alias does not exist.
     * @throws SecureKeyStoreException if SecureKeyStore is not initialized or if
     * the operation fails for some other reason.
     */
    <T extends SecureKey> T getKey(String alias) throws SecureKeyStoreException;

    /**
     * Assigns the given key to the given alias.
     * If the given alias already exists, the keystore information associated
     * with it is overridden by the given key.
     * @param alias the alias name
     * @param key the key to be associated with the alias
     * @throws SecureKeyStoreException if SecureKeyStore is not initialized or the key
     * can't be recovered.
     */
    void setKey(String alias, SecureKey key) throws SecureKeyStoreException;

    /**
     * Returns map of existing keys assiciated with aliases.
     *
     * @param <T> desired type of requested keys
     * @return map of existing keys assiciated with aliases.
     * @throws SecureKeyStoreException if SecureKeyStore is not initialized or if
     * the operation fails for some other reason.
     */
    <T extends SecureKey> Map<String, T> getKeys() throws SecureKeyStoreException;

}



