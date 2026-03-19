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

package org.jpos.q2.cli;

import org.jpos.core.SystemKeyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for CRYPTO command
 */
public class CRYPTOTest {

    @BeforeEach
    void setup() {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("default"), SystemKeyManager.getInstance().generateKey("default"));
    }

    @AfterEach
    void cleanup() {
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("default"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("db"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("api"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("cache"));
    }

    @Test
    public void testCryptoDirectly() throws Exception {
        CRYPTO crypto = new CRYPTO();

        // Test encryption directly
        String input = "my-password";
        String encrypted = crypto.encrypt(input);

        System.out.println("Encrypted: " + encrypted);

        // Verify it's valid output
        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::"), "Output should start with 'enc::'");

        // Extract the base64 part (skip "enc::" prefix = 5 characters)
        String encryptedPart = encrypted.substring(5);

        // Verify it's valid base64
        byte[] decoded = java.util.Base64.getDecoder().decode(encryptedPart);
        assertNotNull(decoded);
        assertTrue(decoded.length > 12); // Should have IV + ciphertext
    }

    @Test
    public void testDifferentEncryptions() throws Exception {
        CRYPTO crypto = new CRYPTO();

        // Each encryption should produce different output (due to random IV)
        String input = "same-password";
        String encrypted1 = crypto.encrypt(input);
        String encrypted2 = crypto.encrypt(input);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    public void testEncryptWithKeyName() throws Exception {
        CRYPTO crypto = new CRYPTO();

        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("db"), SystemKeyManager.getInstance().generateKey("db"));

        String input = "my-password";
        String encrypted = crypto.encrypt(input, "db");

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::db::"), "Output should start with 'enc::db::'");
    }

    @Test
    public void testEncryptWithMultipleKeyNames() throws Exception {
        CRYPTO crypto = new CRYPTO();

        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("db"), SystemKeyManager.getInstance().generateKey("db"));
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("api"), SystemKeyManager.getInstance().generateKey("api"));
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("cache"), SystemKeyManager.getInstance().generateKey("cache"));

        String input = "test-password";
        String encryptedDb = crypto.encrypt(input, "db");
        String encryptedApi = crypto.encrypt(input, "api");
        String encryptedCache = crypto.encrypt(input, "cache");

        assertNotNull(encryptedDb);
        assertNotNull(encryptedApi);
        assertNotNull(encryptedCache);

        assertTrue(encryptedDb.startsWith("enc::db::"));
        assertTrue(encryptedApi.startsWith("enc::api::"));
        assertTrue(encryptedCache.startsWith("enc::cache::"));

        String base64Db = encryptedDb.substring(7);
        String base64Api = encryptedApi.substring(8);
        String base64Cache = encryptedCache.substring(9);

        assertNotEquals(base64Db, base64Api, "Base64 parts should be different for different keys");
        assertNotEquals(base64Api, base64Cache, "Base64 parts should be different for different keys");
        assertNotEquals(base64Db, base64Cache, "Base64 parts should be different for different keys");
    }

}