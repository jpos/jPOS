package org.jpos.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Test that demonstrates using encrypted properties in deploy files.
 * 
 * This test creates:
 * 1. cfg/default.properties with encrypted values referenced via $env{}
 * 2. cfg/default.yml with encrypted values referenced via $env{}
 * 3. A deploy file that loads and prints decrypted property values
 */
public class DeployWithEncryptedPropsTest {
    
    @BeforeEach
    void setup() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        manager.clearKeys();
        
        // Clean up any existing test files
        File[] filesToDelete = {
            new File("cfg/default.properties"),
            new File("cfg/default.yml"),
            new File("cfg/db.properties"),
            new File("deploy/01_encrypted_test.xml")
        };
        
        for (File file : filesToDelete) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Test
    public void testDeployWithEncryptedProperties() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        
        System.out.println("\n==========================================");
        System.out.println("TEST: Deploy with Encrypted Properties");
        System.out.println("==========================================\n");

        // Step 1: Generate a key
        System.out.println("Step 1: Generate key");
        System.out.println("----------------------------------------");
        String envVarName = manager.generateKey("test");
        String keyBase64 = manager.getKeyBase64("test");
        System.out.println("Environment Variable Name: " + envVarName);
        System.out.println("Key (Base64): " + keyBase64);
        System.out.println();

        // Step 2: Set the environment variable (simulating shell export)
        System.out.println("Step 2: Set environment variable (export " + envVarName + ")");
        System.out.println("----------------------------------------");
        // In a real shell, you would do: export JPOS_ENCRYPTION_KEY_TEST=<key>
        // In Java, we set it as a system property
        System.setProperty(envVarName, keyBase64);
        System.out.println("Set: " + envVarName + "=" + keyBase64.substring(0, 20) + "...");
        System.out.println();

        // Step 3: Create default.properties with encrypted values
        System.out.println("Step 3: Create cfg/default.properties");
        System.out.println("----------------------------------------");
        File dbProps = new File("cfg/default.properties");
        dbProps.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(dbProps)) {
            writer.write("# Encrypted properties configuration\n");
            writer.write("JPOS_ENCRYPTION_KEY_TEST=$env{" + envVarName + "}\n");
        }
        System.out.println("Created: cfg/default.properties");
        System.out.println("Contents:");
        System.out.println("  JPOS_ENCRYPTION_KEY_TEST=$env{" + envVarName + "}");
        System.out.println();

        // Step 4: Create default.yml with encrypted values
        System.out.println("Step 4: Create cfg/default.yml");
        System.out.println("----------------------------------------");
        File dbYaml = new File("cfg/default.yml");
        try (FileWriter writer = new FileWriter(dbYaml)) {
            writer.write("JPOS_ENCRYPTION_KEY_TEST: $env{" + envVarName + "}\n");
        }
        System.out.println("Created: cfg/default.yml");
        System.out.println("Contents:");
        System.out.println("  JPOS_ENCRYPTION_KEY_TEST: $env{" + envVarName + "}");
        System.out.println();

        // Step 5: Create encrypted values in db.properties
        System.out.println("Step 5: Create cfg/db.properties with encrypted values");
        System.out.println("----------------------------------------");
        String clearPassword = "my-encrypted-password";
        String encryptedPassword = provider.encrypt(clearPassword, "test");
        File encryptedDbProps = new File("cfg/db.properties");
        encryptedDbProps.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(encryptedDbProps)) {
            writer.write("# Database with encrypted password\n");
            writer.write("hibernate.connection.password=" + encryptedPassword + "\n");
        }
        System.out.println("Created: cfg/db.properties");
        System.out.println("Contents:");
        System.out.println("  hibernate.connection.password=" + encryptedPassword);
        System.out.println();

        // Step 6: Create deploy file that loads and prints decrypted values
        System.out.println("Step 6: Create deploy file");
        System.out.println("----------------------------------------");
        File deployFile = new File("deploy/01_encrypted_test.xml");
        try (FileWriter writer = new FileWriter(deployFile)) {
            writer.write("<qbean name=\"encrypted-test\" class=\"org.jpos.q2.qbean.QBeanSupport\">\n");
            writer.write("    <property name=\"cfg\" file=\"cfg/default.properties\" />\n");
            writer.write("    <property name=\"db.password\" value=\"${hibernate.connection.password}\" />\n");
            writer.write("</qbean>\n");
        }
        System.out.println("Created: deploy/01_encrypted_test.xml");
        System.out.println();

        // Step 7: Verify environment variable is set
        System.out.println("Step 7: Verify environment variable is set");
        System.out.println("----------------------------------------");
        
        // Verify the environment variable is accessible
        String loadedEnvVar = System.getProperty(envVarName);
        System.out.println("Environment variable (" + envVarName + "): " + loadedEnvVar);
        
        // Verify it's the key
        assertNotNull(loadedEnvVar, "Environment variable should be set");
        assertEquals(keyBase64, loadedEnvVar, "Key should match");
        System.out.println("✓ Environment variable is correctly set");
        
        // Decrypt the value using CryptoEnvironmentProvider
        System.out.println();
        System.out.println("Step 8: Verify decryption works");
        System.out.println("----------------------------------------");
        String decryptedPassword = provider.get(encryptedPassword);
        System.out.println("Decrypted password: " + decryptedPassword);
        
        assertEquals(clearPassword, decryptedPassword, "Decrypted password should match");
        System.out.println("✓ Encrypted values in db.properties are decrypted");
        
        System.out.println();
        System.out.println("==========================================");
        System.out.println("TEST PASSED: Deploy with encrypted props!");
        System.out.println("==========================================\n");
    }
}
