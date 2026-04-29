package org.jpos.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentResolveTest {
    @Test
    public void testResolveEnc() {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("default"), SystemKeyManager.getInstance().generateKey("default"));
        System.setProperty("my.test.prop", CryptoEnvironmentProvider.encrypt("secret"));
        String res = Environment.get("${my.test.prop}");
        assertEquals("secret", res);
        System.clearProperty("my.test.prop");
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("default"));
    }
}
