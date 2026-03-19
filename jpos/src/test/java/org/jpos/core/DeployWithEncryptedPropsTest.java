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
    
    @BeforeEach
    void setup() throws Exception {
        SystemKeyManager manager = SystemKeyManager.getInstance();
        keyBase64 = manager.generateKey("test");
        envVarName = manager.getEnvVarName("test");
        System.setProperty(envVarName, keyBase64);
    }
    
    @AfterEach
    void cleanup() {
        System.clearProperty(envVarName);
    }
    
    @Test
    public void testDeployWithEncryptedProperties() throws Exception {
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        
        String clearPassword = "my-encrypted-password";
        String encryptedPassword = provider.encrypt(clearPassword, "test");
        
        File cfgDir = tempDir.resolve("cfg").toFile();
        cfgDir.mkdirs();
        
        File dbProps = new File(cfgDir, "db.properties");
        try (FileWriter writer = new FileWriter(dbProps)) {
            writer.write("# Database with encrypted password\n");
            writer.write("hibernate.connection.password=" + encryptedPassword + "\n");
        }
        
        File deployDir = tempDir.resolve("deploy").toFile();
        deployDir.mkdirs();
        
        File deployFile = new File(deployDir, "01_encrypted_test.xml");
        try (FileWriter writer = new FileWriter(deployFile)) {
            writer.write("<qbean name=\"encrypted-test\" class=\"org.jpos.q2.qbean.QBeanSupport\">\n");
            writer.write("    <property name=\"cfg\" file=\"" + cfgDir.getAbsolutePath() + "/db.properties\" />\n");
            writer.write("    <property name=\"db.password\" value=\"${hibernate.connection.password}\" />\n");
            writer.write("</qbean>\n");
        }
        
        assertTrue(dbProps.exists(), "db.properties should exist");
        assertTrue(deployFile.exists(), "deploy file should exist");
        
        String decryptedPassword = provider.get(encryptedPassword);
        assertEquals(clearPassword, decryptedPassword, "Decrypted password should match");
    }
}
