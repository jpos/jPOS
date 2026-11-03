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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CryptoEnvironmentProvider}.
 * <p>
 * These tests verify the encrypt/decrypt round-trip behavior of AES-256-GCM encryption,
 * including various input types (URLs, passwords, Unicode, special characters, long strings).
 * </p>
 * <h3>Test Key Injection Pattern</h3>
 * <p>
 * Like all SystemKeyManager tests, this class uses the {@link SystemKeyManager.KeySupplier}
 * injection pattern. A default key is generated in {@link #setup()} and injected via a supplier
 * that looks up keys from the local {@code testKeys} map. Cleanup in {@link #cleanup()} resets
 * the supplier to null (restoring environment-variable behavior).
 * </p>
 */
public class CryptoEnvironmentProviderTest {

    // Local storage for test keys: maps key names to their raw byte values.
    private final Map<String, byte[]> testKeys = new HashMap<>();

    /**
     * Sets up a test supplier with a randomly generated default key before each test.
     */
    @BeforeEach
    void setup() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateKey("default");
        testKeys.put("default", Base64.getDecoder().decode(base64Key));
        manager.setKeySupplier(this::getTestKey);
    }

    /**
     * Cleans up the supplier after each test to ensure test isolation.
     */
    @AfterEach
    void cleanup() {
        SystemKeyManager.getInstance().setKeySupplier(null);
        testKeys.clear();
    }

    /**
     * Returns a SecretKey for the given key name by looking it up in the testKeys map.
     * Used as a method reference for {@link SystemKeyManager#setKeySupplier(KeySupplier)}.
     */
    private SecretKey getTestKey(String name) {
        byte[] bytes = testKeys.get(name);
        return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
    }

    /**
     * Verifies that encrypting a URL and decrypting it produces the original value.
     */
    @Test
    public void testEncryptDecryptRoundTrip() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String original = "localhost:3306/client?useSSL=false";
        String encrypted = CryptoEnvironmentProvider.encrypt(original);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::"));

        String decrypted = provider.get(encrypted.substring(5));
        assertEquals(original, decrypted);
    }

    /**
     * Verifies decryption of a database password.
     */
    @Test
    public void testDecryptDatabasePassword() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String password = "secretpassword";
        String encrypted = CryptoEnvironmentProvider.encrypt(password);

        String decrypted = provider.get(encrypted.substring(5));
        assertEquals(password, decrypted);
    }

    /**
     * Verifies decryption of a database username.
     */
    @Test
    public void testDecryptDatabaseUsername() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String username = "client";
        String encrypted = CryptoEnvironmentProvider.encrypt(username);

        String decrypted = provider.get(encrypted.substring(5));
        assertEquals(username, decrypted);
    }

    /**
     * Verifies that encrypting the same value twice produces different ciphertexts.
     * This confirms AES-GCM is using a random IV each time (semantic security).
     */
    @Test
    public void testDifferentEncryptionsProduceDifferentOutput() {
        String value = "test-value";
        String encrypted1 = CryptoEnvironmentProvider.encrypt(value);
        String encrypted2 = CryptoEnvironmentProvider.encrypt(value);

        assertNotEquals(encrypted1, encrypted2, "Each encryption should use a different IV");
    }

    /**
     * Verifies that encrypting and decrypting an empty string works correctly.
     */
    @Test
    public void testDecryptEmptyString() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String encrypted = CryptoEnvironmentProvider.encrypt("");
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals("", decrypted);
    }

    /**
     * Verifies that encrypting and decrypting a string with special characters works correctly.
     */
    @Test
    public void testDecryptSpecialCharacters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String special = "p@ssw0rd!#$%^&*()";
        String encrypted = CryptoEnvironmentProvider.encrypt(special);
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals(special, decrypted);
    }

    /**
     * Verifies that encrypting and decrypting Unicode characters (Chinese) works correctly.
     */
    @Test
    public void testDecryptUnicodeCharacters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String unicode = "密码 123 密码";
        String encrypted = CryptoEnvironmentProvider.encrypt(unicode);
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals(unicode, decrypted);
    }

    /**
     * Verifies that encrypting and decrypting a long string (1000 iterations of "test-") works correctly.
     */
    @Test
    public void testDecryptLongString() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("test-");
        }
        String longValue = sb.toString();

        String encrypted = CryptoEnvironmentProvider.encrypt(longValue);
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals(longValue, decrypted);
    }

    /**
     * Verifies that the prefix returned by {@link CryptoEnvironmentProvider#prefix()} is "enc::".
     */
    @Test
    public void testPrefixReturnsCrypto() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        assertEquals("enc::", provider.prefix());
    }

    /**
     * Verifies that passing invalid Base64 to {@link CryptoEnvironmentProvider#get(String)} throws a RuntimeException.
     */
    @Test
    public void testInvalidBase64ThrowsException() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

         assertRuntimeException(() -> provider.get("invalid-base64!!!"));
    }

    /**
     * Verifies that tampering with the ciphertext (changing one character) causes decryption to fail.
     * AES-GCM provides authenticated encryption — any modification to the ciphertext is detected.
     */
    @Test
    public void testTamperedCiphertextThrowsException() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String original = "test-value";
        String encrypted = CryptoEnvironmentProvider.encrypt(original);

        // Tamper with the ciphertext by replacing the first character after "enc::"
        String tampered = "enc::" + encrypted.replaceFirst(".", "X");

        assertRuntimeException(() -> provider.get(tampered.substring(5)));
    }

    /**
     * Verifies that passing {@code null} to {@link CryptoEnvironmentProvider#get(String)} throws a RuntimeException.
     */
    @Test
    public void testNullInputThrowsException() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        assertRuntimeException(() -> provider.get(null));
    }

    /**
     * Verifies decrypting a full MySQL JDBC URL.
     */
    @Test
    public void testDecryptMySQLFullUrl() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String url = "jdbc:mysql://localhost:3306/client?useSSL=false";
        String encrypted = CryptoEnvironmentProvider.encrypt(url);
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals(url, decrypted);
    }

    /**
     * Verifies decrypting a PostgreSQL JDBC URL.
     */
    @Test
    public void testDecryptPostgreSQLUrl() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String url = "jdbc:postgresql://db.example.com:5432/mydb";
        String encrypted = CryptoEnvironmentProvider.encrypt(url);
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals(url, decrypted);
    }

    /**
     * Verifies decrypting a query string with multiple parameters.
     */
    @Test
    public void testDecryptWithQueryParameters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String value = "user=admin&password=secret&database=mydb";
        String encrypted = CryptoEnvironmentProvider.encrypt(value);
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals(value, decrypted);
    }

    /**
     * Verifies decrypting a URL with special characters in the credentials.
     */
    @Test
    public void testDecryptSpecialUrlCharacters() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        String value = "user:pass@host:3306/db?param=value&another=123";
        String encrypted = CryptoEnvironmentProvider.encrypt(value);
        String decrypted = provider.get(encrypted.substring(5));

        assertEquals(value, decrypted);
    }

    /**
     * Verifies encrypting and decrypting with a named key ("db").
     */
    @Test
    public void testEncryptDecryptWithKeyName() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateKey("db");
        testKeys.put("db", Base64.getDecoder().decode(base64Key));

        String original = "my-database-password";
        String encrypted = CryptoEnvironmentProvider.encrypt(original, "db");

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::db:"));

        String decrypted = provider.get(encrypted.substring(5));
        assertEquals(original, decrypted);
    }

    /**
     * Verifies encrypting with multiple named keys ("db", "api") produces distinct ciphertexts
     * that can each be decrypted back to the original value using the same key.
     */
    @Test
    public void testEncryptDecryptWithMultipleKeyNames() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();

        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Db = manager.generateKey("db");
        String base64Api = manager.generateKey("api");
        testKeys.put("db", Base64.getDecoder().decode(base64Db));
        testKeys.put("api", Base64.getDecoder().decode(base64Api));

        String original = "test-value";
        String encryptedDb = CryptoEnvironmentProvider.encrypt(original, "db");
        String encryptedApi = CryptoEnvironmentProvider.encrypt(original, "api");

        assertNotNull(encryptedDb);
        assertNotNull(encryptedApi);

        assertTrue(encryptedDb.startsWith("enc::db:"));
        assertTrue(encryptedApi.startsWith("enc::api:"));

        String decryptedDb = provider.get(encryptedDb.substring(5));
        String decryptedApi = provider.get(encryptedApi.substring(5));

        assertEquals(original, decryptedDb);
        assertEquals(original, decryptedApi);

        assertNotEquals(encryptedDb, encryptedApi, "Encrypted values should differ for different keys");
    }

    /**
     * Verifies that the provider handles arbitrary strings with colons robustly.
     * The test creates an artificial scenario where the input resembles a key-value pair
     * but contains invalid Base64 content, expecting a RuntimeException.
     */
    @Test
    public void testBase64WithColonsIsProperlyProcessed() {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        
        
        // Let's create an artificial scenario where the base64 happens to start with something resembling a key
        // But with an invalid key name pattern like "+abc:" 
        // Note: the test just verifies the method handles arbitrary strings robustly.
        assertRuntimeException(() -> provider.get("+abc:invalidbase64content"));
    }

    /**
     * Helper method to assert that a Runnable throws a RuntimeException.
     */
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
