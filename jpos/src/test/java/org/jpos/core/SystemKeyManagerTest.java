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
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class SystemKeyManagerTest {

    private static final String DEFAULT = "default";

    @AfterEach
    void cleanup() {
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName(DEFAULT));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("key1"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("key2"));
    }

    @Test
    public void testGetInstanceReturnsSingleton() {
        SystemKeyManager instance1 = SystemKeyManager.getInstance();
        SystemKeyManager instance2 = SystemKeyManager.getInstance();

        assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

    @Test
    public void testGetDefaultKeyReturnsKey() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String base64Key = manager.generateDefaultKey();
        System.setProperty(manager.getEnvVarName(DEFAULT), base64Key);

        SecretKey key = manager.getDefaultKey();
        assertNotNull(key, "getDefaultKey() should return a key when property is set");
        assertEquals(256, key.getEncoded().length * 8, "Key should be 256 bits");
        assertEquals("AES", key.getAlgorithm(), "Key algorithm should be AES");
    }

    @Test
    public void testGetKeyReturnsKey() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String base64Key = manager.generateKey(DEFAULT);
        System.setProperty(manager.getEnvVarName(DEFAULT), base64Key);

        SecretKey key = manager.getKey(DEFAULT);
        assertNotNull(key, "getKey() should return a key when property is set");
    }

    @Test
    public void testGetKeyBase64ReturnsValidBase64() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String base64Key = manager.generateKey(DEFAULT);
        System.setProperty(manager.getEnvVarName(DEFAULT), base64Key);

        String fetchedBase64Key = manager.getKeyBase64(DEFAULT);
        assertNotNull(fetchedBase64Key, "getKeyBase64() should return a string after property is set");
        assertFalse(fetchedBase64Key.isEmpty(), "Base64 string should not be empty");
        assertEquals(base64Key, fetchedBase64Key);

        byte[] decoded = Base64.getDecoder().decode(fetchedBase64Key);
        assertNotNull(decoded, "Base64 should be decodable");
        assertEquals(32, decoded.length, "Decoded key should be 32 bytes (256 bits)");
    }

    @Test
    public void testEnvironmentVariableIsSet() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateDefaultKey();

        String envVarName = manager.getEnvVarName(DEFAULT);

        // User must set the environment variable manually
        System.setProperty(envVarName, base64Key);

        String envVar = System.getProperty(envVarName);

        assertNotNull(envVar, "Environment variable should be set after user sets it");
        assertFalse(envVar.isEmpty(), "Environment variable should not be empty");

        byte[] decoded = Base64.getDecoder().decode(envVar);
        assertNotNull(decoded, "Environment variable should contain valid Base64");
        assertEquals(32, decoded.length, "Environment variable should contain 32 bytes");
    }

    @Test
    public void testKeyConsistencyAcrossInstances() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        System.setProperty(manager.getEnvVarName(DEFAULT), manager.generateDefaultKey());

        SecretKey key1 = manager.getDefaultKey();
        SecretKey key2 = manager.getDefaultKey();

        assertArrayEquals(key1.getEncoded(), key2.getEncoded(), "Keys should be identical across calls");
    }

    @Test
    public void testKeyLengthIs256Bits() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        System.setProperty(manager.getEnvVarName(DEFAULT), manager.generateDefaultKey());

        SecretKey key = manager.getDefaultKey();
        int keyLengthBits = key.getEncoded().length * 8;
        assertEquals(256, keyLengthBits, "Key length should be 256 bits");
    }

    @Test
    public void testGetEnvVarName() {
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String envVar = manager.getEnvVarName(DEFAULT);
        assertEquals("JPOS_ENCRYPTION_KEY", envVar, "Default env var name should be JPOS_ENCRYPTION_KEY");
    }

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
