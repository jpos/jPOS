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

public class CryptoEnvironmentProviderTest {

    @BeforeEach
    void setup() {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("default"), SystemKeyManager.getInstance().generateKey("default"));
    }

    @AfterEach
    void cleanup() {
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("default"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("db"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("api"));
    }

    @Test
    public void testEncryptDecryptRoundTrip() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String original = "localhost:3306/client?useSSL=false";
        String encrypted = CryptoEnvironmentProvider.encrypt(original);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::"));

        String decrypted = provider.get(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testDecryptDatabasePassword() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String password = "secretpassword";
        String encrypted = CryptoEnvironmentProvider.encrypt(password);

        String decrypted = provider.get(encrypted);
        assertEquals(password, decrypted);
    }

    @Test
    public void testDecryptDatabaseUsername() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String username = "client";
        String encrypted = CryptoEnvironmentProvider.encrypt(username);

        String decrypted = provider.get(encrypted);
        assertEquals(username, decrypted);
    }

    @Test
    public void testDifferentEncryptionsProduceDifferentOutput() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String value = "test-value";
        String encrypted1 = CryptoEnvironmentProvider.encrypt(value);
        String encrypted2 = CryptoEnvironmentProvider.encrypt(value);

        assertNotEquals(encrypted1, encrypted2, "Each encryption should use a different IV");
    }

    @Test
    public void testDecryptEmptyString() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String encrypted = CryptoEnvironmentProvider.encrypt("");
        String decrypted = provider.get(encrypted);

        assertEquals("", decrypted);
    }

    @Test
    public void testDecryptSpecialCharacters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String special = "p@ssw0rd!#$%^&*()";
        String encrypted = CryptoEnvironmentProvider.encrypt(special);
        String decrypted = provider.get(encrypted);

        assertEquals(special, decrypted);
    }

    @Test
    public void testDecryptUnicodeCharacters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String unicode = "密码123密码";
        String encrypted = CryptoEnvironmentProvider.encrypt(unicode);
        String decrypted = provider.get(encrypted);

        assertEquals(unicode, decrypted);
    }

    @Test
    public void testDecryptLongString() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("test-");
        }
        String longValue = sb.toString();

        String encrypted = CryptoEnvironmentProvider.encrypt(longValue);
        String decrypted = provider.get(encrypted);

        assertEquals(longValue, decrypted);
    }

    @Test
    public void testPrefixReturnsCrypto() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        assertEquals("enc::", provider.prefix());
    }

    @Test
    public void testInvalidBase64ThrowsException() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        assertRuntimeException(() -> {
            provider.get("invalid-base64!!!");
        });
    }

    @Test
    public void testTamperedCiphertextThrowsException() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String original = "test-value";
        String encrypted = CryptoEnvironmentProvider.encrypt(original);

        // Tamper with the ciphertext
        String tampered = "enc::" + encrypted.replaceFirst(".", "X");

        assertRuntimeException(() -> {
            provider.get(tampered);
        });
    }

    @Test
    public void testNullInputThrowsException() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        assertRuntimeException(() -> {
            provider.get(null);
        });
    }

    @Test
    public void testDecryptMySQLFullUrl() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String url = "jdbc:mysql://localhost:3306/client?useSSL=false";
        String encrypted = CryptoEnvironmentProvider.encrypt(url);
        String decrypted = provider.get(encrypted);

        assertEquals(url, decrypted);
    }

    @Test
    public void testDecryptPostgreSQLUrl() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String url = "jdbc:postgresql://db.example.com:5432/mydb";
        String encrypted = CryptoEnvironmentProvider.encrypt(url);
        String decrypted = provider.get(encrypted);

        assertEquals(url, decrypted);
    }

    @Test
    public void testDecryptWithQueryParameters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String value = "user=admin&password=secret&database=mydb";
        String encrypted = CryptoEnvironmentProvider.encrypt(value);
        String decrypted = provider.get(encrypted);

        assertEquals(value, decrypted);
    }

    @Test
    public void testDecryptSpecialUrlCharacters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String value = "user:pass@host:3306/db?param=value&another=123";
        String encrypted = CryptoEnvironmentProvider.encrypt(value);
        String decrypted = provider.get(encrypted);

        assertEquals(value, decrypted);
    }

    @Test
    public void testEncryptDecryptWithKeyName() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        // Generate the key first
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("db"), SystemKeyManager.getInstance().generateKey("db"));

        String original = "my-database-password";
        String encrypted = CryptoEnvironmentProvider.encrypt(original, "db");

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::db::"));

        String decrypted = provider.get(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptDecryptWithMultipleKeyNames() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        // Generate keys first
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("db"), SystemKeyManager.getInstance().generateKey("db"));
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("api"), SystemKeyManager.getInstance().generateKey("api"));

        String original = "test-value";
        String encryptedDb = CryptoEnvironmentProvider.encrypt(original, "db");
        String encryptedApi = CryptoEnvironmentProvider.encrypt(original, "api");

        assertNotNull(encryptedDb);
        assertNotNull(encryptedApi);

        assertTrue(encryptedDb.startsWith("enc::db::"));
        assertTrue(encryptedApi.startsWith("enc::api::"));

        String decryptedDb = provider.get(encryptedDb);
        String decryptedApi = provider.get(encryptedApi);

        assertEquals(original, decryptedDb);
        assertEquals(original, decryptedApi);

        assertNotEquals(encryptedDb, encryptedApi, "Encrypted values should differ for different keys");
    }

    // Helper method to assert RuntimeException
    private void assertRuntimeException(Runnable runnable) {
        try {
            runnable.run();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            // Expected
            assertNotNull(e.getMessage());
        }
    }

}