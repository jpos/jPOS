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

package org.jpos.core;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EnvironmentProvider that encrypts/decrypts values using AES256.
 * 
 * <p>
 * Format: {@code enc::<base64-encoded-ciphertext>}
 * <ul>
 * <li>Algorithm: AES-256-GCM with authenticated encryption</li>
 * <li>Key: 256-bit AES key loaded from environment variable via SystemKeyManager</li>
 * <li>IV/Nonce: 12 bytes (generated per encryption)</li>
 * <li>Authentication: 16-byte GCM tag included in ciphertext</li>
 * </ul>
 */
public class CryptoEnvironmentProvider implements EnvironmentProvider {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ENC_PREFIX = "enc::";
    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String prefix() {
        return ENC_PREFIX;
    }

    @Override
    public String get(String config) {
        try {
            String keyName = null;
            String encoded;

            int colonIdx = config.indexOf(':');
            if (colonIdx >= 0) {
                keyName = config.substring(0, colonIdx);
                encoded = config.substring(colonIdx + 1);
            } else {
                encoded = config;
            }

            byte[] decoded = Base64.getDecoder().decode(encoded);
            ByteBuffer buf = ByteBuffer.wrap(decoded);

            // First 12 bytes are the IV/nonce
            byte[] iv = new byte[IV_SIZE_BYTES];
            buf.get(iv);

            // Rest is ciphertext with GCM authentication tag
            byte[] ciphertext = new byte[buf.remaining()];
            buf.get(ciphertext);

            // Use SystemKeyManager to get the derived key
            SecretKey key = SystemKeyManager.getInstance().getKey(keyName);
            if (key == null) {
                throw new RuntimeException("Key not found in environment for name: " + 
                        (keyName != null && !keyName.isEmpty() ? keyName : "default") + 
                        ". Please set " + SystemKeyManager.getInstance().getEnvVarName(keyName));
            }
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), ALGORITHM);

            // Decrypt with GCM authentication
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt value", e);
        }
    }

    /**
     * Helper method to encrypt a value (for generating encrypted config).
     * 
     * @param value the plaintext value to encrypt
     * @return base64-encoded ciphertext with enc:: prefix
     */
    public static String encrypt(String value) {
        return encrypt(value, null);
    }
    
    /**
     * Helper method to encrypt a value with a named key.
     * 
     * @param value the plaintext value to encrypt
     * @param keyName the name of the key to use (null for default)
     * @return base64-encoded ciphertext with enc::keyname: prefix
     */
    public static String encrypt(String value, String keyName) {
        try {
            SecretKey key = SystemKeyManager.getInstance().getKey(keyName);
            if (key == null) {
                throw new IllegalArgumentException("Key not found in environment for name: " + 
                        (keyName != null && !keyName.isEmpty() ? keyName : "default") + 
                        ". Please set " + SystemKeyManager.getInstance().getEnvVarName(keyName));
            }
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), ALGORITHM);

            // Generate secure random 12-byte IV
            byte[] iv = new byte[IV_SIZE_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            // Encrypt with GCM authentication
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);

            String base64 = Base64.getEncoder().encodeToString(buf.array());
            
            // If keyName is provided, include it in the prefix
            if (keyName != null && !keyName.isEmpty()) {
                 return ENC_PREFIX + keyName + ":" + base64;
             } else {
                 return ENC_PREFIX + base64;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt value", e);
        }
    }
}
