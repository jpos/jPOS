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

import org.jpos.q2.CLI;
import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.CRYPTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CRYPTO} CLI command.
 * <p>
 * The CRYPTO command allows users to encrypt secrets via the jPOS CLI:
 * <pre>{@code
 * crypto "my-secret-password"          # encrypt with default key
 * crypto "my-secret-password" db       # encrypt with "db" named key
 * }</pre>
 * </p>
 * <h3>Test Key Injection Pattern</h3>
 * <p>
 * These tests use the same {@link SystemKeyManager.KeySupplier} injection pattern as other
 * SystemKeyManager tests. The supplier is set up in {@link #setup()} with a default key,
 * and individual test methods add additional named keys as needed via {@code testKeys.put()}.
 * </p>
 * <p>
 * Cleanup happens in {@link #cleanup()}, which resets the supplier to null (restoring
 * environment-variable behavior) and clears the local test key map.
 * </p>
 */
public class CRYPTOTest {

    // Local storage for test keys: maps key names to their raw byte values.
    // Populated in setup() with the default key, and extended by individual test methods.
    private final Map<String, byte[]> testKeys = new HashMap<>();

    /**
     * Sets up a test supplier with a randomly generated default key before each test.
     * This ensures every test starts with a valid encryption key without relying on
     * environment variables.
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
     * This method is used as a method reference for {@link SystemKeyManager#setKeySupplier(KeySupplier)}.
     *
     * @param name the key name to look up
     * @return the SecretKey, or null if not found
     */
    private SecretKey getTestKey(String name) {
        byte[] bytes = testKeys.get(name);
        return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
    }

    /**
     * Verifies that the CRYPTO command encrypts a secret and produces output
     * starting with {@code enc::}, which is the prefix used by {@link CryptoEnvironmentProvider}.
     * <p>
     * The test then decrypts the output to verify the round-trip produces the original input.
     * </p>
     */
    @Test
    public void testCryptoCommandEncryptsDirectly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        
        CLI cliObj = new CLI(null, in, out, null, false, false);

        CLIContext cli = CLIContext.builder()
                .cli(cliObj)
                .out(out)
                .build();

        CRYPTO cmd = new CRYPTO();
        
        // args[0] = "crypto", args[1] = "my-secret-password"
        cmd.exec(cli, new String[]{"crypto", "my-secret-password"});
        
        String output = out.toString().trim();
        assertNotNull(output);
        assertTrue(output.startsWith("enc::"), "Output should start with 'enc::'");
        
        // Decrypt the output to verify the round-trip produces the original input
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        String decrypted = provider.get(output.substring(5));
        assertEquals("my-secret-password", decrypted, "Decrypted text should match the CLI input");
    }

    /**
     * Verifies that the CRYPTO command with a named key ("db") produces output
     * starting with {@code enc::db:}, which includes the key name in the ciphertext.
     */
    @Test
    public void testCryptoCommandWithKeyName() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateKey("db");
        testKeys.put("db", Base64.getDecoder().decode(base64Key));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        
        CLI cliObj = new CLI(null, in, out, null, false, false);

        CLIContext cli = CLIContext.builder()
                .cli(cliObj)
                .out(out)
                .build();

        CRYPTO cmd = new CRYPTO();
        
        // args[0] = "crypto", args[1] = "my-secret-password", args[2] = "db"
        cmd.exec(cli, new String[]{"crypto", "my-secret-password", "db"});
        
        String output = out.toString().trim();
        assertNotNull(output);
        assertTrue(output.startsWith("enc::db:"), "Output should start with 'enc::db:'");
        
        // Decrypt to verify the round-trip produces the original input
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        String decrypted = provider.get(output.substring(5));
        assertEquals("my-secret-password", decrypted, "Decrypted text should match the CLI input");
    }

    /**
     * Verifies that the CRYPTO command rejects too many arguments by printing usage instructions.
     */
    @Test
    public void testCryptoCommandTooManyArguments() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        
        CLI cliObj = new CLI(null, in, out, null, false, false);

        CLIContext cli = CLIContext.builder()
                .cli(cliObj)
                .out(out)
                .build();

        CRYPTO cmd = new CRYPTO();
        
        // passing 4 arguments: args[0]="crypto", args[1]="secret", args[2]="db", args[3]="extra"
        cmd.exec(cli, new String[]{"crypto", "my-secret-password", "db", "extra"});
        
        String output = out.toString().trim();
        assertTrue(output.contains("Usage: crypto"), "Output should contain usage instructions when given too many arguments");
    }

    /**
     * Verifies that {@link CryptoEnvironmentProvider#encrypt(String)} produces valid encrypted output
     * with the correct prefix and a ciphertext long enough to indicate real encryption.
     */
    @Test
    public void testCryptoDirectly() throws Exception {
        String input = "my-password";
        String encrypted = CryptoEnvironmentProvider.encrypt(input);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::"), "Output should start with 'enc::'");

        String encryptedPart = encrypted.substring(5);

        byte[] decoded = java.util.Base64.getDecoder().decode(encryptedPart);
        assertNotNull(decoded);
        assertTrue(decoded.length > 12, "Ciphertext should be longer than just the plaintext");
    }

    /**
     * Verifies that encrypting the same value twice produces different ciphertexts.
     * This confirms that AES-GCM is using a random IV (initialization vector) each time,
     * which is essential for semantic security — identical plaintexts should not produce
     * identical ciphertexts.
     */
    @Test
    public void testDifferentEncryptions() throws Exception {
        String input = "same-password";
        String encrypted1 = CryptoEnvironmentProvider.encrypt(input);
        String encrypted2 = CryptoEnvironmentProvider.encrypt(input);

        assertNotEquals(encrypted1, encrypted2);
    }

    /**
     * Verifies that encrypting with a named key ("db") produces output starting with
     * {@code enc::db:}, confirming the key name is embedded in the ciphertext prefix.
     */
    @Test
    public void testEncryptWithKeyName() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateKey("db");
        testKeys.put("db", Base64.getDecoder().decode(base64Key));

        String input = "my-password";
        String encrypted = CryptoEnvironmentProvider.encrypt(input, "db");

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::db:"), "Output should start with 'enc::db:'");
    }

    /**
     * Verifies that encrypting with multiple named keys ("db", "api", "cache") produces
     * distinct ciphertexts for each key. This confirms that different keys produce
     * different encrypted outputs even when the plaintext is identical.
     */
    @Test
    public void testEncryptWithMultipleKeyNames() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Db = manager.generateKey("db");
        String base64Api = manager.generateKey("api");
        String base64Cache = manager.generateKey("cache");
        testKeys.put("db", Base64.getDecoder().decode(base64Db));
        testKeys.put("api", Base64.getDecoder().decode(base64Api));
        testKeys.put("cache", Base64.getDecoder().decode(base64Cache));

        String input = "test-password";
        String encryptedDb = CryptoEnvironmentProvider.encrypt(input, "db");
        String encryptedApi = CryptoEnvironmentProvider.encrypt(input, "api");
        String encryptedCache = CryptoEnvironmentProvider.encrypt(input, "cache");

        assertNotNull(encryptedDb);
        assertNotNull(encryptedApi);
        assertNotNull(encryptedCache);

        assertTrue(encryptedDb.startsWith("enc::db:"));
        assertTrue(encryptedApi.startsWith("enc::api:"));
        assertTrue(encryptedCache.startsWith("enc::cache:"));

        // Extract the ciphertext portion (after the key name prefix)
        String encryptedPartDb = encryptedDb.substring(7);
        String encryptedPartApi = encryptedApi.substring(8);
        String encryptedPartCache = encryptedCache.substring(9);

        assertNotEquals(encryptedPartDb, encryptedPartApi, "Base64 parts should be different for different keys");
        assertNotEquals(encryptedPartApi, encryptedPartCache, "Base64 parts should be different for different keys");
        assertNotEquals(encryptedPartDb, encryptedPartCache, "Base64 parts should be different for different keys");
    }

}
