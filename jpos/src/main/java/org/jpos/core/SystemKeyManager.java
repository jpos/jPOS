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

import org.jpos.util.Log;
import org.jpos.q2.Q2;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Manages AES-256 encryption keys loaded from environment variables.
 *
 * <p>
 * This class is responsible for loading, generating, and resolving AES-256
 * encryption keys used by jPOS to encrypt sensitive configuration values
 * (database passwords, API keys, etc.).
 * </p>
 *
 * <h3>Key Loading Strategy</h3>
 * <p>
 * Keys are loaded strictly from environment variables at lookup time.
 * No internal caching is performed — each call to {@link #getKey(String)}
 * performs a fresh lookup via the current {@link KeySupplier}. This ensures
 * that key changes are always visible and prevents stale keys from persisting
 * within a single JVM process.
 * </p>
 * <h4>Key Rotation Limitation</h4>
 * <p>
 * Because the default supplier reads from {@code System.getenv()}, environment
 * variable changes take effect only after a JVM restart. The JVM caches its
 * environment variables at startup, so changing an environment variable in a
 * running process will not affect key resolution. To rotate keys in production,
 * deploy a new process instance with the updated environment variables.
 * </p>
 * <p>
 * Real-time key rotation is possible only by providing a custom
 * {@link KeySupplier} (e.g., one that polls an external secrets manager or
 * KMS on each call). The default supplier does not support this.
 * </p>
 *
 * <h3>Key Naming Convention</h3>
 * <p>
 * Each key is identified by a name string. The default key uses the name
 * {@code "default"} (or null/empty). Additional keys can be created with
 * arbitrary names like {@code "db"}, {@code "api"}, or {@code "cache"}.
 * </p>
 * <p>
 * The environment variable name is derived from the key name using
 * {@link #getEnvVarName(String)}:
 * <ul>
 *   <li>Default key: {@code JPOS_ENCRYPTION_KEY}</li>
 *   <li>Named key "db": {@code JPOS_ENCRYPTION_KEY_DB}</li>
 *   <li>Named key "my-key": {@code JPOS_ENCRYPTION_KEY_MY_KEY}</li>
 * </ul>
 * Non-alphanumeric characters in the key name are normalized to underscores.
 * </p>
 *
 * <h3>Key Supplier Pattern</h3>
 * <p>
 * Key resolution is delegated to a {@link KeySupplier} functional interface,
 * enabling clean separation between production and test behavior:
 * </p>
 * <ul>
 *   <li><b>Production:</b> The default supplier ({@link EnvKeySupplier}) loads
 *       keys from environment variables. This is the only code that accesses
 *       {@code System.getenv()}, ensuring encryption keys can never be set
 *       at runtime via {@code System.setProperty()} — a security backdoor
 *       that was removed in a previous revision.</li>
 *   <li><b>Tests:</b> Test code within {@code org.jpos.core} injects a custom
 *       supplier via {@link #setKeySupplier(KeySupplier)} that returns
 *       deterministic, pre-generated keys. This eliminates the need for test
 *       infrastructure (maps, internal caches) to leak into production code.</li>
 * </ul>
 * <p>
 * The supplier field is {@code volatile} and null-safe: setting it to
 * {@code null} restores the default environment-variable behavior.
 * </p>
 *
 * <h3>Security Considerations</h3>
 * <p>
 * Encryption keys MUST be set as environment variables before the JVM starts,
 * NOT via {@code System.setProperty()} at runtime. Any code running in the
 * same JVM can call {@code System.setProperty()}, which would allow a
 * compromised component to inject or replace encryption keys — defeating
 * the entire purpose of encrypting sensitive configuration values.
 * </p>
 * <p>
 * The default supplier accesses only {@code System.getenv()}, which is
 * immutable after process start. This provides a clear security boundary:
 * keys are set once at startup and cannot be modified from within Java code.
 * </p>
 *
 * @see KeySupplier
 */
public class SystemKeyManager {
    // Logger for key-related warnings (invalid Base64, wrong key length, etc.)
    private static final Log log = Log.getLog(Q2.LOGGER_NAME, "crypto-env-provider");

    // The default key name — used when no specific key name is provided
    private static final String DEFAULT_KEY_NAME = "default";

    // Base environment variable name for the default encryption key
    private static final String DEFAULT_ENV_VAR = "JPOS_ENCRYPTION_KEY";

    // AES-256 requires exactly 32 bytes (256 bits)
    private static final int KEY_SIZE_BITS = 256;

    // Singleton instance — initialized eagerly with the default env-var-based supplier
    private static final SystemKeyManager instance = new SystemKeyManager();

    // The current key supplier. Volatile for thread-safe visibility across threads.
    // null means "use default EnvKeySupplier" (lazy fallback in getKey()).
    private volatile KeySupplier keySupplier;

    /**
     * Private constructor — initializes with the default environment-variable-based supplier.
     */
    private SystemKeyManager() {
        this.keySupplier = new EnvKeySupplier();
    }

    /**
     * Returns the singleton SystemKeyManager instance.
     * <p>
     * This is a true singleton — all callers receive the same instance.
     * </p>
     *
     * @return the SystemKeyManager instance
     */
    public static SystemKeyManager getInstance() {
        return instance;
    }

    /**
     * Sets a custom KeySupplier for key resolution.
     * <p>
     * This method is package-private and intended solely for use by test code
     * within the {@code org.jpos.core} package to inject deterministic keys
     * instead of relying on environment variables. It is not part of the
     * public production API.
     * </p>
     * <h4>Production Use</h4>
     * <p>
     * In production, do not call this method. The default supplier loads keys
     * from environment variables only — never from {@code System.getProperty()}.
     * If real-time key rotation is needed (e.g., polling a KMS), configure it
     * through your deployment infrastructure by providing a custom supplier
     * at application startup via the appropriate initialization mechanism.
     * </p>
     * <h4>Test Use</h4>
     * <p>
     * In tests, inject a supplier that returns pre-generated test keys:
     * </p>
     * <pre>{@code
     * // Generate a key and store it for the test
     * String base64Key = manager.generateKey("default");
     * byte[] keyBytes = Base64.getDecoder().decode(base64Key);
     *
     * // Inject the supplier — all subsequent getKey() calls will return this key
     * manager.setKeySupplier(name -> new SecretKeySpec(keyBytes, "AES"));
     *
     * // ... run tests ...
     *
     * // Restore default behavior (loads from environment variables)
     * manager.setKeySupplier(null);
     * }</pre>
     * <p>
     * The supplier field is {@code volatile} so changes are immediately
     * visible across all threads. This is important because encryption/decryption
     * operations may occur in different threads than the one that set the supplier.
     * </p>
     *
     * @param keySupplier the supplier to use for key resolution; pass {@code null}
     *                    to restore the default environment-variable-based behavior
     */
    void setKeySupplier(KeySupplier keySupplier) {
        this.keySupplier = keySupplier;
    }

    /**
     * Gets a SecretKey by name. Returns {@code null} if no key is found.
     * <p>
     * Key resolution is delegated to the current {@link KeySupplier}.
     * No caching is performed — each call performs a fresh lookup.
     * </p>
     * <h4>Behavior</h4>
     * <ul>
     *   <li>If {@code keyName} is {@code null} or empty, resolves the default key.</li>
     *   <li>The current supplier is read into a local variable to avoid stale reads
     *       due to concurrent updates to the volatile field (double-checked idiom).</li>
     *   <li>If the supplier is {@code null}, a fresh {@link EnvKeySupplier} is created
     *       as a fallback — this restores default environment-variable behavior.</li>
     * </ul>
     * <h4>Security Note</h4>
     * <p>
     * In production, the supplier always loads from {@code System.getenv()} only.
     * There is NO fallback to {@code System.getProperty()}. This prevents runtime
     * key injection via {@code System.setProperty()}, which would be a security vulnerability.
     * </p>
     *
     * @param keyName the name of the key to get; {@code null} or empty resolves to the default key
     * @return the SecretKey, or {@code null} if not found
     */
    public SecretKey getKey(String keyName) {
        // Normalize: null/empty key names resolve to the default key
        if (keyName == null || keyName.isEmpty()) {
            keyName = DEFAULT_KEY_NAME;
        }

        // Read volatile field into local variable to avoid stale reads.
        // This pattern prevents a race where the supplier is swapped between
        // the null check and the actual get() call.
        KeySupplier supplier = keySupplier;
        if (supplier == null) {
            // Restore default behavior: load from environment variables
            supplier = new EnvKeySupplier();
        }

        return supplier.get(keyName);
    }

    /**
     * Gets the default encryption key. Convenience method that delegates to {@link #getKey(String)}.
     * <p>
     * Package-private — intended for internal use within the {@code org.jpos.core} package.
     * </p>
     *
     * @return the default SecretKey, or {@code null} if not found
     */
    SecretKey getDefaultKey() {
        return getKey(DEFAULT_KEY_NAME);
    }

    /**
     * Gets the Base64-encoded representation of a key by name.
     * <p>
     * This is useful for displaying keys or storing them in configuration files.
     * The returned string can be decoded back into a SecretKey using
     * {@link #getKey(String)} (via the supplier) or manually via Base64 decoding.
     * </p>
     * <p>
     * Package-private — intended for internal use within the {@code org.jpos.core} package.
     * </p>
     *
     * @param keyName the name of the key
     * @return Base64-encoded key string, or {@code null} if the key is not found
     */
    String getKeyBase64(String keyName) {
        SecretKey key = getKey(keyName);
        return key != null ? Base64.getEncoder().encodeToString(key.getEncoded()) : null;
    }

    /**
     * Generates a new random AES-256 key using OS-provided SecureRandom entropy.
     * <p>
     * This method generates a cryptographically strong random key but does NOT
     * set any environment variable or store the key anywhere. The caller is
     * responsible for setting the environment variable manually (or injecting
     * the key via package-private {@link #setKeySupplier(KeySupplier)} in test code).
     * </p>
     * <h4>Usage</h4>
     * <pre>{@code
     * String base64Key = manager.generateKey("db");
     * System.out.println("Set this environment variable:");
     * System.out.println("  JPOS_ENCRYPTION_KEY_DB=" + base64Key);
     * }</pre>
     *
     * @param keyName the name to give the key (used for naming the environment variable)
     * @return the generated key as a Base64-encoded string (32 bytes, URL-safe)
     * @throws RuntimeException if the AES key generator fails to initialize
     */
    public String generateKey(String keyName) {
        try {
            // Initialize AES-256 key generator with OS-provided SecureRandom
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE_BITS, new SecureRandom());
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key", e);
        }
    }

    /**
     * Generates a new random AES-256 key for the default key name.
     * <p>
     * Convenience method that delegates to {@link #generateKey(String)} with
     * the default key name ({@code "default"}). The corresponding environment
     * variable is {@code JPOS_ENCRYPTION_KEY}.
     * </p>
     *
     * @return the generated default key as a Base64-encoded string
     */
    public String generateDefaultKey() {
        return generateKey(DEFAULT_KEY_NAME);
    }

    /**
     * Gets the environment variable name for a given key name.
     * <p>
     * The naming convention is:
     * <ul>
     *   <li>Default key (null/empty/"default"): {@code JPOS_ENCRYPTION_KEY}</li>
     *   <li>Named key "db": {@code JPOS_ENCRYPTION_KEY_DB}</li>
     *   <li>Named key "my-key": {@code JPOS_ENCRYPTION_KEY_MY_KEY}</li>
     * </ul>
     * Non-alphanumeric characters in the key name are replaced with underscores,
     * and the result is uppercased to conform to environment variable naming conventions.
     * </p>
     * <h4>Examples</h4>
     * <pre>{@code
     * getEnvVarName(null)          -> "JPOS_ENCRYPTION_KEY"
     * getEnvVarName("")            -> "JPOS_ENCRYPTION_KEY"
     * getEnvVarName("default")     -> "JPOS_ENCRYPTION_KEY"
     * getEnvVarName("db")          -> "JPOS_ENCRYPTION_KEY_DB"
     * getEnvVarName("my-key")      -> "JPOS_ENCRYPTION_KEY_MY_KEY"
     * getEnvVarName("api_key 123!") -> "JPOS_ENCRYPTION_KEY_API_KEY_123_"
     * }</pre>
     *
     * @param keyName the name of the key; {@code null} or empty resolves to the default
     * @return the environment variable name (e.g., {@code "JPOS_ENCRYPTION_KEY_DB"})
     */
    public String getEnvVarName(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            keyName = DEFAULT_KEY_NAME;
        }
        // Build env var name: prefix + "_" + sanitized key name (uppercased, non-alphanumeric replaced with _)
        return DEFAULT_ENV_VAR + (DEFAULT_KEY_NAME.equals(keyName) ? "" : "_" + keyName.toUpperCase().replaceAll("[^A-Z0-9]", "_"));
    }

    /**
     * Functional interface for resolving encryption keys by name.
     * <p>
     * This interface abstracts the source of encryption keys, enabling:
     * <ul>
     *   <li><b>Production:</b> Load keys from environment variables ({@link EnvKeySupplier})</li>
     *   <li><b>Tests:</b> Return deterministic, pre-generated keys via lambda or method reference</li>
     * </ul>
     * </p>
     * <h4>Implementation Contract</h4>
     * <p>
     * Implementations must return a valid AES SecretKey for the given name,
     * or {@code null} if no key is found. The returned key's encoded bytes
     * must be exactly 32 bytes (AES-256). Invalid keys are rejected by callers.
     * </p>
     * <h4>Thread Safety</h4>
     * <p>
     * Implementations should be thread-safe, as {@link SystemKeyManager#getKey(String)}
     * may be called from multiple threads concurrently. The default supplier
     * ({@link EnvKeySupplier}) is inherently thread-safe because it only reads
     * environment variables (which are immutable after process start).
     * </p>
     */
    @FunctionalInterface
    public interface KeySupplier {
    /**
          * Returns the SecretKey for the given name, or {@code null} if not found.
          * <p>
          * This method is called by {@link SystemKeyManager#getKey(String)} for
          * every key lookup. Implementations should perform a fresh lookup each time —
          * no caching is expected or required. For real-time key rotation (e.g., polling
          * an external secrets manager), implement this interface and set it via
          * {@link SystemKeyManager#setKeySupplier(KeySupplier)}. Note that the default
          * supplier ({@link EnvKeySupplier}) loads from environment variables, which are
          * immutable within a running JVM process.
          * </p>
          *
          * @param keyName the name of the key to resolve; never {@code null} (null/empty names are normalized by caller)
          * @return the SecretKey for the given name, or {@code null} if not found
          */
        SecretKey get(String keyName);
    }

    /**
     * Default KeySupplier that loads keys from environment variables.
     * <p>
     * This is the production supplier used when no custom supplier is set via
     * {@link #setKeySupplier(KeySupplier)}. It reads keys from environment variables
     * using the naming convention defined by {@link #getEnvVarName(String)}.
     * Environment variables are immutable within a running JVM process; changes
     * require a restart to take effect.
     * </p>
     * <h4>Security Boundary</h4>
     * <p>
     * This supplier accesses ONLY {@code System.getenv()} — never {@code System.getProperty()}.
     * Environment variables are set before the JVM starts and cannot be modified from within
     * Java code, providing a clear security boundary against runtime key injection.
     * </p>
     * <h4>Error Handling</h4>
     * <p>
     * If the Base64 decoding fails or the key length is incorrect, a warning is logged
     * via {@link Log#warn(Object)} and {@code null} is returned (same as "key not found").
     * This prevents exceptions from crashing the application while still alerting operators.
     * </p>
     */
    private class EnvKeySupplier implements KeySupplier {
        @Override
        public SecretKey get(String keyName) {
            // Look up the environment variable name for this key
            String envVarName = SystemKeyManager.this.getEnvVarName(keyName);

            // Read from environment variable — this is IMMUTABLE after process start.
            // We intentionally do NOT fall back to System.getProperty() here.
            // A System.getProperty fallback would be a security vulnerability because
            // any code in the JVM could call System.setProperty() to inject/replace keys.
            String envValue = System.getenv(envVarName);

            if (envValue != null && !envValue.trim().isEmpty()) {
                try {
                    byte[] keyBytes = Base64.getDecoder().decode(envValue.trim());

                    // Validate key length: AES-256 requires exactly 32 bytes
                    if (keyBytes.length == KEY_SIZE_BITS / 8) {
                        return new SecretKeySpec(keyBytes, "AES");
                    } else {
                        log.warn("Invalid key length in " + envVarName + ": expected " + (KEY_SIZE_BITS / 8) + " bytes, got " + keyBytes.length);
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid Base64 encoding — log warning and return null (same as "not found")
                    log.warn("Invalid Base64 in " + envVarName + ": " + e.getMessage());
                }
            }

            // Key not found, invalid, or malformed — return null
            return null;
        }
    }
}
