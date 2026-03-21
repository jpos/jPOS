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

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Manages AES-256 encryption keys loaded from environment variables.
 * 
 * <p>
 * This class:
 * <ul>
 * <li>Loads keys strictly from environment variables (no internal caching)</li>
 * <li>Generates new keys using OS-provided SecureRandom (truly random)</li>
 * </ul>
 */
public class SystemKeyManager {
    private static final String DEFAULT_KEY_NAME = "default";
    private static final String DEFAULT_ENV_VAR = "JPOS_ENCRYPTION_KEY";
    private static final int KEY_SIZE_BITS = 256;

    private static final SystemKeyManager instance;

    static {
        try {
            instance = new SystemKeyManager();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SystemKeyManager", e);
        }
    }

    private SystemKeyManager() {
    }

    /**
     * Returns the singleton SystemKeyManager instance.
     *
     * @return the SystemKeyManager instance
     */
    public static SystemKeyManager getInstance() {
        return instance;
    }

    /**
     * Gets a key by name. Returns null if key doesn't exist in environment.
     * Never caches the key.
     *
     * @param keyName the name of the key to get
     * @return the SecretKey, or null if not found
     */
    public SecretKey getKey(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            keyName = DEFAULT_KEY_NAME;
        }

        String envVarName = getEnvVarName(keyName);
        String envValue = System.getenv(envVarName);

        // Fallback for test environments where setting System.getenv is not possible
        if (envValue == null || envValue.trim().isEmpty()) {
            envValue = System.getProperty(envVarName);
        }

        if (envValue != null && !envValue.trim().isEmpty()) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(envValue.trim());
                if (keyBytes.length == KEY_SIZE_BITS / 8) {
                    return new SecretKeySpec(keyBytes, "AES");
                } else {
                    java.util.logging.Logger.getLogger(SystemKeyManager.class.getName())
                            .warning("Invalid key length in environment variable " + envVarName + ". Expected " + (KEY_SIZE_BITS / 8) + " bytes, got " + keyBytes.length);
                }
            } catch (IllegalArgumentException e) {
                java.util.logging.Logger.getLogger(SystemKeyManager.class.getName())
                        .warning("Invalid Base64 in environment variable " + envVarName + ": " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Gets the default key. Returns null if key doesn't exist.
     *
     * @return the default SecretKey, or null if not found
     */
    public SecretKey getDefaultKey() {
        return getKey(DEFAULT_KEY_NAME);
    }

    /**
     * Gets the Base64-encoded key by name.
     *
     * @param keyName the name of the key
     * @return Base64-encoded key, or null if not found
     */
    public String getKeyBase64(String keyName) {
        SecretKey key = getKey(keyName);
        return key != null ? Base64.getEncoder().encodeToString(key.getEncoded()) : null;
    }

    /**
     * Generates a new random key using OS-provided SecureRandom entropy.
     * The user is responsible for setting the environment variable manually.
     *
     * @param keyName the name to give the key
     * @return the generated key in Base64
     */
    public String generateKey(String keyName) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE_BITS, new SecureRandom());
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key", e);
        }
    }

    /**
     * Generates a new default key from SystemSeed.
     *
     * @return the Base64-encoded generated key
     */
    public String generateDefaultKey() {
        return generateKey(DEFAULT_KEY_NAME);
    }

    /**
     * Gets the environment variable name for a key.
     * Non-alphanumeric characters in the key name are normalized to underscores.
     *
     * @param keyName the name of the key
     * @return the environment variable name
     */
    public String getEnvVarName(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            keyName = DEFAULT_KEY_NAME;
        }
        return DEFAULT_ENV_VAR + (DEFAULT_KEY_NAME.equals(keyName) ? "" : "_" + keyName.toUpperCase().replaceAll("[^A-Z0-9]", "_"));
    }

    
}
