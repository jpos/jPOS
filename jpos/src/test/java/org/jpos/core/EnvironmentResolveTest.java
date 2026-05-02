package org.jpos.core;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for environment-based property resolution with encrypted values.
 * <p>
 * This test verifies that the {@link Environment} class correctly resolves
 * encrypted properties (prefixed with "enc::") when they are set as system properties.
 * The encryption/decryption uses {@link CryptoEnvironmentProvider}, which in turn
 * relies on {@link SystemKeyManager} to load the encryption key.
 * </p>
 * <h3>Test Key Injection</h3>
 * <p>
 * Since tests cannot rely on environment variables, we inject a test key via
 * {@link SystemKeyManager#setKeySupplier(KeySupplier)}. The supplier looks up keys
 * from the local {@code testKeys} map.
 * </p>
 */
public class EnvironmentResolveTest {

    // Local storage for test keys: maps key names to their raw byte values.
    private final Map<String, byte[]> testKeys = new HashMap<>();

    /**
     * Verifies that the Environment class correctly resolves an encrypted property.
     * <p>
     * The flow is:
     * <ol>
     *   <li>Generate a test key and inject it via {@code setKeySupplier}</li>
     *   <li>Encrypt a secret value using {@link CryptoEnvironmentProvider#encrypt(String)}</li>
     *   <li>Set the encrypted value as a system property</li>
     *   <li>Resolve the property via {@link Environment#get(String)} with a placeholder reference</li>
     *   <li>Verify the decrypted value matches the original secret</li>
     * </ol>
     * </p>
     */
    @Test
    public void testResolveEnc() {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        String base64Key = manager.generateKey("default");
        testKeys.put("default", Base64.getDecoder().decode(base64Key));

        // Inject the test key supplier — all subsequent getKey() calls will return our test key
        manager.setKeySupplier(name -> {
            byte[] bytes = testKeys.get(name);
            return bytes != null ? new SecretKeySpec(bytes, "AES") : null;
        });

        // Encrypt a secret and set it as a system property
        System.setProperty("my.test.prop", CryptoEnvironmentProvider.encrypt("secret"));

        // Resolve the property through the Environment — this should automatically decrypt it
        String res = Environment.get("${my.test.prop}");
        assertEquals("secret", res);

        // Clean up: remove the system property and reset the supplier
        System.clearProperty("my.test.prop");
        manager.setKeySupplier(null);
        testKeys.clear();
    }
}
