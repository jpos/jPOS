package org.jpos.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DeployWithEncryptedPropsTest {
    
    @TempDir
    Path tempDir;
    
    private String envVarName;
    private String keyBase64;
    private String originalEnvDir;
    private String originalEnv;
    
    @BeforeEach
    void setup() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        keyBase64 = manager.generateKey("test");
        envVarName = manager.getEnvVarName("test");
        System.setProperty(envVarName, keyBase64);
        
        originalEnvDir = System.getProperty("jpos.envdir");
        originalEnv = System.getProperty("jpos.env");
    }
    
    @AfterEach
    void cleanup() throws Exception {
        System.clearProperty(envVarName);
        
        if (originalEnvDir != null) {
            System.setProperty("jpos.envdir", originalEnvDir);
        } else {
            System.clearProperty("jpos.envdir");
        }
        
        if (originalEnv != null) {
            System.setProperty("jpos.env", originalEnv);
        } else {
            System.clearProperty("jpos.env");
        }
        
        Environment.reload();
    }
    
    @Test
    public void testDeployWithEncryptedProperties() throws Exception {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        
        String clearPassword = "my-encrypted-password";
        String encryptedPassword = provider.encrypt(clearPassword, "test");
        
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
        
        // Evaluate the property through the Environment resolution exactly as Q2 would
        String decryptedPassword = Environment.get("${hibernate.connection.password}");
        assertEquals(clearPassword, decryptedPassword, "Environment should automatically decrypt the password");
    }
}
