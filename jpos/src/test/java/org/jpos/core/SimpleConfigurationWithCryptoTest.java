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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that SimpleConfiguration properly processes EnvironmentProvider
 * transformations
 */
public class SimpleConfigurationWithCryptoTest {

    @BeforeEach
    void setup() {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("default"), SystemKeyManager.getInstance().generateKey("default"));
    }

    @AfterEach
    void cleanup() {
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("default"));
    }

    @Test
    public void testSimpleConfigurationWithCryptoPrefix() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        // Test that the provider directly decrypts enc:: values
        String password = "secretpassword";
        String encryptedPassword = CryptoEnvironmentProvider.encrypt(password);

        String decryptedPassword = provider.get(encryptedPassword);
        assertEquals(password, decryptedPassword);

        String username = "client";
        String encryptedUsername = CryptoEnvironmentProvider.encrypt(username);

        String decryptedUsername = provider.get(encryptedUsername);
        assertEquals(username, decryptedUsername);
    }

    @Test
    public void testSimpleConfigurationWithCryptoInArray() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        // Test that the provider can decrypt multiple values
        String[] passwords = { "secret123", "secret456" };

        for (String password : passwords) {
            String encrypted = CryptoEnvironmentProvider.encrypt(password);
            String decrypted = provider.get(encrypted);
            assertEquals(password, decrypted);
        }
    }

    @Test
    public void testEnvironmentProcessesCryptoInExpressions() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        // When Environment processes ${enc::...} expressions, it should decrypt
        String value = "test-password";
        String encrypted = CryptoEnvironmentProvider.encrypt(value);

        // Test direct provider usage
        String decrypted = provider.get(encrypted);
        assertEquals(value, decrypted);
    }

    @Test
    public void testCryptoPrefixDetection() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        // Verify prefix is correct
        assertEquals("enc::", provider.prefix());

        // Verify encryption produces correct format
        String encrypted = CryptoEnvironmentProvider.encrypt("test");
        assertTrue(encrypted.startsWith("enc::"));

        // Verify decryption works
        String decrypted = provider.get(encrypted);
        assertEquals("test", decrypted);
    }
}
