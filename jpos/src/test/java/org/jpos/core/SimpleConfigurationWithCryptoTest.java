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
        props.setProperty("db.password", encryptedPassword);

        SimpleConfiguration cfg = new SimpleConfiguration(props);

        assertEquals(password, cfg.get("db.password"));
    }
}
