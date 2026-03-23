package org.jpos.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleConfigurationWithCryptoTest {

    @BeforeEach
    void setup() {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("default"), SystemKeyManager.getInstance().generateKey("default"));
    }

    @AfterEach
    void cleanup() {
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("default"));
    }

    @Test
    public void testSimpleConfigurationGet() throws Exception {
        String password = "secretpassword";
        String encryptedPassword = CryptoEnvironmentProvider.encrypt(password);

        Properties props = new Properties();
        // Environment evaluates ${...} properties by resolving their references
        // (from env/sys/cfg) and applying EnvironmentProviders if there's a matching prefix (like enc::)
        System.setProperty("db.password.encrypted", encryptedPassword);
        props.setProperty("db.password", "${db.password.encrypted}");

        SimpleConfiguration cfg = new SimpleConfiguration(props);

        assertEquals(password, cfg.get("db.password"));
        
        System.clearProperty("db.password.encrypted");
    }
}
