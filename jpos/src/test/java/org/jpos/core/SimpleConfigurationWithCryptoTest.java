package org.jpos.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SimpleConfiguration} with encrypted property values.
 * <p>
 * This test verifies that {@code SimpleConfiguration} correctly resolves encrypted
 * properties when they reference other system properties containing encrypted values.
 * </p>
 * <h3>How It Works</h3>
 * <p>
 * The test creates a scenario where:
 * <ol>
 *   <li>A secret password is encrypted using {@link CryptoEnvironmentProvider#encrypt(String)}</li>
 *   <li>The encrypted value is set as a system property ({@code db.password.encrypted})</li>
 *   <li>A {@code SimpleConfiguration} maps {@code db.password} to a placeholder reference: {@code ${db.password.encrypted}}</li>
 *   <li>When {@code cfg.get("db.password")} is called, the configuration resolves the placeholder,
 *       reads the system property, and decrypts the value</li>
 * </ol>
 * </p>
 * <h3>Test Key Injection</h3>
 * <p>
 * The encryption key is injected via {@link SystemKeyManager#setKeySupplier(KeySupplier)} in
 * {@link #setup()}, and cleaned up in {@link #cleanup()}.
 * </p>
 */
public class SimpleConfigurationWithCryptoTest {

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

        // Inject the test key supplier — all subsequent getKey() calls will return our test key
        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });
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
     * Verifies that SimpleConfiguration correctly resolves an encrypted property reference.
     * <p>
     * The flow:
     * <ol>
     *   <li>Encrypt a password using CryptoEnvironmentProvider</li>
     *   <li>Set the encrypted value as a system property</li>
     *   <li>Create a SimpleConfiguration that maps db.password to ${db.password.encrypted}</li>
     *   <li>Call cfg.get("db.password") — this should resolve the placeholder and decrypt</li>
     *   <li>Verify the result matches the original plaintext password</li>
     * </ol>
     * </p>
     */
    @Test
    public void testSimpleConfigurationGet() throws Exception {
        String password = "secretpassword";

        // Encrypt the password — produces a string like "enc::default:base64-ciphertext"
        String encryptedPassword = CryptoEnvironmentProvider.encrypt(password);

        Properties props = new Properties();
        // Environment evaluates ${...} properties by resolving their references
        // (from env/sys/cfg) and applying EnvironmentProviders if there's a matching prefix (like enc::)
        System.setProperty("db.password.encrypted", encryptedPassword);
        props.setProperty("db.password", "${db.password.encrypted}");

        SimpleConfiguration cfg = new SimpleConfiguration(props);

        // This should resolve the placeholder, read the system property, and decrypt
        assertEquals(password, cfg.get("db.password"));
        
        System.clearProperty("db.password.encrypted");
    }
}
