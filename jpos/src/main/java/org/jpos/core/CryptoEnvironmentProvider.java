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

import org.jpos.iso.ISOUtil;
import org.jpos.security.SystemSeed;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EnvironmentProvider that encrypts/decrypts values using AES256.
 * 
 * <p>
 * Format: {@code crypto::<base64-encoded-ciphertext>}
 * <ul>
 * <li>Algorithm: AES-256-GCM with authenticated encryption</li>
 * <li>Key: 32 bytes derived from SystemSeed using SHA-256</li>
 * <li>IV/Nonce: 12 bytes (generated per encryption)</li>
 * <li>Authentication: 16-byte GCM tag included in ciphertext</li>
 * </ul>
 */
public class CryptoEnvironmentProvider implements EnvironmentProvider {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE_BITS = 256;
    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    @Override
    public String prefix() {
        return "crypto::";
    }

    @Override
    public String get(String config) {
        try {
            // Remove the crypto:: prefix if present
            String encoded = config;
            if (config.startsWith("crypto::")) {
                encoded = config.substring(8);
            }

            byte[] decoded = Base64.getDecoder().decode(encoded);
            ByteBuffer buf = ByteBuffer.wrap(decoded);

            // First 12 bytes are the IV/nonce
            byte[] iv = new byte[IV_SIZE_BYTES];
            buf.get(iv);

            // Rest is ciphertext with GCM authentication tag
            byte[] ciphertext = new byte[buf.remaining()];
            buf.get(ciphertext);

            // Use first 32 bytes of SystemSeed as the 256-bit AES key
            byte[] seed = SystemSeed.getSeed(0, 32);
            SecretKeySpec keySpec = new SecretKeySpec(seed, ALGORITHM);

            // Decrypt with GCM authentication
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt value", e);
        }
    }

    /**
     * Helper method to encrypt a value (for generating encrypted config).
     * 
     * @param value the plaintext value to encrypt
     * @return base64-encoded ciphertext with crypto:: prefix
     */
    public static String encrypt(String value) {
        try {
            // Use first 32 bytes of SystemSeed as the 256-bit AES key
            byte[] seed = SystemSeed.getSeed(0, 32);
            SecretKeySpec keySpec = new SecretKeySpec(seed, ALGORITHM);

            // Generate secure random 12-byte IV
            byte[] iv = new byte[IV_SIZE_BYTES];
            new SecureRandom().nextBytes(iv);

            // Encrypt with GCM authentication
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] ciphertext = cipher.doFinal(value.getBytes());

            // Combine IV and ciphertext
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);

            return "crypto::" + Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt value", e);
        }
    }
}