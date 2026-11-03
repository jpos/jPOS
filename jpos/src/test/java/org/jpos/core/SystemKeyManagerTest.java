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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SystemKeyManager}.
 * <p>
 * These tests verify key generation, resolution, and the environment variable naming convention.
 * </p>
 * <h3>Test Key Injection Pattern</h3>
 * <p>
 * Since {@code SystemKeyManager} is a singleton with no internal test caches (production code
 * must never leak test infrastructure), tests inject keys via the {@link SystemKeyManager.KeySupplier}
 * interface. The pattern used in every test method is:
 * </p>
 * <ol>
 *   <li>Generate a random key using {@code manager.generateKey(name)}</li>
 *   <li>Decode the Base64 string to raw bytes</li>
 *   <li>Store the bytes in a local {@code Map<String, byte[]> testKeys}</li>
 *   <li>Inject a supplier that looks up keys from this map: {@code manager.setKeySupplier(name -> ...)}</li>
 *   <li>Run assertions against {@code manager.getKey(name)}</li>
 *   <li>Clean up in {@link #cleanup()}: reset supplier to null and clear the map</li>
 * </ol>
 * <p>
 * The {@code cleanup()} method runs after every test via {@literal @}AfterEach, ensuring test isolation.
 * Setting the supplier back to {@code null} restores default environment-variable behavior for any
 * subsequent tests that might depend on it.
 * </p>
 */
public class SystemKeyManagerTest {

    // The key name used across all tests — "default" is the built-in default
    private static final String DEFAULT = "default";

    // Local storage for test keys: maps key names to their raw byte values.
    // This map is populated in each test method and cleared after every test.
    // It exists ONLY in test code — never in production.
    private final Map<String, byte[]> testKeys = new HashMap<>();

    /**
     * Cleans up the supplier after each test to ensure test isolation.
     * <p>
     * Setting the supplier to {@code null} restores default environment-variable behavior.
     * Clearing the map releases the test key bytes and prevents memory leaks.
     * </p>
     */
    @AfterEach
    void cleanup() {
        SystemKeyManager.getInstance().setKeySupplier(null);
        testKeys.clear();
    }

    /**
     * Verifies that {@link SystemKeyManager#getInstance()} returns the same instance
     * on every call — confirming the singleton pattern.
     */
    @Test
    public void testGetInstanceReturnsSingleton() {
        SystemKeyManager instance1 = SystemKeyManager.getInstance();
        SystemKeyManager instance2 = SystemKeyManager.getInstance();

        assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

    /**
     * Verifies that {@link SystemKeyManager#getDefaultKey()} returns a valid key
     * when a supplier is configured.
     */
    @Test
    public void testGetDefaultKeyReturnsKey() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        // Generate a random default key and store it for the test
        String base64Key = manager.generateDefaultKey();
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        testKeys.put(DEFAULT, keyBytes);

        // Inject supplier — all getKey() calls will now return our test key
        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });

        SecretKey key = manager.getDefaultKey();
        assertNotNull(key, "getDefaultKey() should return a key when supplier is set");
        assertEquals(256, key.getEncoded().length * 8, "Key should be 256 bits");
        assertEquals("AES", key.getAlgorithm(), "Key algorithm should be AES");
    }

    /**
     * Verifies that {@link SystemKeyManager#getKey(String)} returns a valid key
     * when the supplier is configured with that key name.
     */
    @Test
    public void testGetKeyReturnsKey() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String base64Key = manager.generateKey(DEFAULT);
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        testKeys.put(DEFAULT, keyBytes);

        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });

        SecretKey key = manager.getKey(DEFAULT);
        assertNotNull(key, "getKey() should return a key when supplier is set");
    }

    /**
     * Verifies that {@link SystemKeyManager#getKeyBase64(String)} returns a valid Base64 string
     * matching the originally generated key.
     */
    @Test
    public void testGetKeyBase64ReturnsValidBase64() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String base64Key = manager.generateKey(DEFAULT);
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        testKeys.put(DEFAULT, keyBytes);

        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });

        String fetchedBase64Key = manager.getKeyBase64(DEFAULT);
        assertNotNull(fetchedBase64Key, "getKeyBase64() should return a string after supplier is set");
        assertFalse(fetchedBase64Key.isEmpty(), "Base64 string should not be empty");
        assertEquals(base64Key, fetchedBase64Key);

        byte[] decoded = Base64.getDecoder().decode(fetchedBase64Key);
        assertNotNull(decoded, "Base64 should be decodable");
        assertEquals(32, decoded.length, "Decoded key should be 32 bytes (256 bits)");
    }

    /**
     * Verifies that keys injected via the supplier are correctly retrievable.
     * This test confirms the end-to-end flow: generate → store in map → inject supplier → retrieve.
     */
    @Test
    public void testEnvironmentVariableIsSet() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateDefaultKey();
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        testKeys.put(DEFAULT, keyBytes);

        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });

        SecretKey key = manager.getKey(DEFAULT);
        assertNotNull(key, "Key should be set after using supplier");
        assertEquals(32, key.getEncoded().length, "Decoded key should be 32 bytes (256 bits)");
    }

    /**
     * Verifies that the same key is returned consistently across multiple calls.
     * This tests that the supplier returns a new SecretKeySpec wrapping the same underlying bytes
     * each time — so the encoded content is identical even though the object reference differs.
     */
    @Test
    public void testKeyConsistencyAcrossInstances() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateDefaultKey();
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        testKeys.put(DEFAULT, keyBytes);

        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });

        SecretKey key1 = manager.getDefaultKey();
        SecretKey key2 = manager.getDefaultKey();

        assertArrayEquals(key1.getEncoded(), key2.getEncoded(), "Keys should be identical across calls");
    }

    /**
     * Verifies that keys injected via the supplier are exactly 256 bits (32 bytes).
     */
    @Test
    public void testKeyLengthIs256Bits() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateDefaultKey();
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        testKeys.put(DEFAULT, keyBytes);

        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });

        SecretKey key = manager.getDefaultKey();
        int keyLengthBits = key.getEncoded().length * 8;
        assertEquals(256, keyLengthBits, "Key length should be 256 bits");
    }

    /**
     * Verifies that the default environment variable name is {@code JPOS_ENCRYPTION_KEY}.
     */
    @Test
    public void testGetEnvVarName() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String envVar = manager.getEnvVarName(DEFAULT);
        assertEquals("JPOS_ENCRYPTION_KEY", envVar, "Default env var name should be JPOS_ENCRYPTION_KEY");
    }

    /**
     * Verifies the environment variable naming convention:
     * <ul>
     *   <li>Default key → {@code JPOS_ENCRYPTION_KEY}</li>
     *   <li>"db" → {@code JPOS_ENCRYPTION_KEY_DB}</li>
     *   <li>"my-key" → {@code JPOS_ENCRYPTION_KEY_MY_KEY} (hyphens normalized to underscores)</li>
     *   <li>"api_key 123!" → {@code JPOS_ENCRYPTION_KEY_API_KEY_123_} (special chars replaced)</li>
     * </ul>
     */
    @Test
    public void testGetEnvVarNameSanitization() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        assertEquals("JPOS_ENCRYPTION_KEY", manager.getEnvVarName(DEFAULT));
        assertEquals("JPOS_ENCRYPTION_KEY_DB", manager.getEnvVarName("db"));
        assertEquals("JPOS_ENCRYPTION_KEY_MY_KEY", manager.getEnvVarName("my-key"));
        assertEquals("JPOS_ENCRYPTION_KEY_API_KEY_123_", manager.getEnvVarName("api_key 123!"));
        assertEquals("JPOS_ENCRYPTION_KEY_XYZ_", manager.getEnvVarName("XYZ#")); // Testing user's exact example
    }

}
