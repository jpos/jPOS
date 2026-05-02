package org.jpos.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for deploying jPOS configuration files with encrypted properties.
 * <p>
 * This test simulates the real-world scenario where a jPOS Q2 instance loads
 * configuration from {@code .cfg} files in a {@code cfg/} directory, and those
 * files contain encrypted values (e.g., database passwords prefixed with "enc::").
 * </p>
 * <h3>How It Works</h3>
 * <p>
 * The test:
 * <ol>
 *   <li>Creates a temporary directory with a {@code cfg/} subdirectory</li>
 *   <li>Writes a {@code db.cfg} file containing an encrypted password</li>
 *   <li>Sets system properties to point jPOS at the temporary directory: {@code jpos.envdir} and {@code jpos.env}</li>
 *   <li> reloads the Environment so it picks up the configuration file</li>
 *   <li>Resolves the password via {@link Environment#get(String)} — this should automatically decrypt it</li>
 * </ol>
 * </p>
 * <h3>Test Key Injection</h3>
 * <p>
 * The encryption key for the "test" key name is injected via
 * {@link SystemKeyManager#setKeySupplier(KeySupplier)} in {@link #setup()}.
 * Cleanup in {@link #cleanup()} restores original system properties and resets the supplier.
 * </p>
 */
public class DeployWithEncryptedPropsTest {
    
    @TempDir
    Path tempDir;
    
    // Local storage for test keys: maps key names to their raw byte values.
    private final Map<String, byte[]> testKeys = new HashMap<>();

    // Original values of system properties that we save and restore after the test.
    // This prevents tests from polluting the global system property state.
    private String originalEnvDir;
    private String originalEnv;
    
    /**
     * Sets up a test supplier with a randomly generated key for the "test" key name,
     * and saves the original values of jpos.system properties to restore later.
     */
    @BeforeEach
    void setup() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateKey("test");
        testKeys.put("test", Base64.getDecoder().decode(base64Key));

        // Inject the test key supplier — all subsequent getKey() calls will return our test key for "test"
        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });
        
        // Save original values so we can restore them after the test
        originalEnvDir = System.getProperty("jpos.envdir");
        originalEnv = System.getProperty("jpos.env");
    }
    
    /**
     * Cleans up: resets the key supplier, restores original system properties,
     * and reloads the Environment to clear any cached state from this test.
     */
    @AfterEach
    void cleanup() throws Exception {
        SystemKeyManager.getInstance().setKeySupplier(null);
        testKeys.clear();
        
        // Restore original environment directory setting
        if (originalEnvDir != null) {
            System.setProperty("jpos.envdir", originalEnvDir);
        } else {
            System.clearProperty("jpos.envdir");
        }
        
        // Restore original environment profile setting
        if (originalEnv != null) {
            System.setProperty("jpos.env", originalEnv);
        } else {
            System.clearProperty("jpos.env");
        }
        
        // Reload the Environment to clear any cached state from this test
        Environment.reload();
    }
    
    /**
     * Verifies that jPOS correctly loads and decrypts properties from a {@code .cfg} file.
     * <p>
     * This test simulates the real-world scenario where:
     * <ul>
     *   <li>A {@code cfg/db.cfg} file contains encrypted database credentials</li>
     *   <li>jPOS loads this file when {@code jpos.envdir} and {@code jpos.env} are set</li>
     *   <li>The Environment automatically decrypts values prefixed with "enc::"</li>
     * </ul>
     * </p>
     */
    @Test
    public void testDeployWithEncryptedProperties() throws Exception {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        
        // Encrypt a password using the "test" key name
        String clearPassword = "my-encrypted-password";
        String encryptedPassword = provider.encrypt(clearPassword, "test");
        
        // Create a temporary cfg directory with a db.cfg file containing the encrypted password
        File cfgDir = tempDir.resolve("cfg").toFile();
        cfgDir.mkdirs();
        
        // Write .cfg instead of .properties so Environment will load it directly
        File dbProps = new File(cfgDir, "db.cfg");
        try (FileWriter writer = new FileWriter(dbProps)) {
            writer.write("hibernate.connection.password=" + encryptedPassword + "\n");
        }
        
        // Instruct Environment to load from our temporary cfg dir and "db" profile
        System.setProperty("jpos.envdir", cfgDir.getAbsolutePath());
        System.setProperty("jpos.env", "db");
        Environment.reload();
        
        // Evaluate the property through the Environment resolution exactly as Q2 would.
        // This should automatically decrypt the password (the value starts with "enc::test:")
        String decryptedPassword = Environment.get("${hibernate.connection.password}");
        assertEquals(clearPassword, decryptedPassword, "Environment should automatically decrypt the password");
    }
}
