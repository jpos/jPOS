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

package org.jpos.q2.cli;

import org.jpos.core.CryptoEnvironmentProvider;
import org.jpos.core.SystemKeyManager;
import org.jpos.q2.CLI;
import org.jpos.q2.CLIContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for CRYPTO command
 */
public class CRYPTOTest {

    @BeforeEach
    void setup() {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("default"), SystemKeyManager.getInstance().generateKey("default"));
    }

    @AfterEach
    void cleanup() {
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("default"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("db"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("api"));
        System.clearProperty(SystemKeyManager.getInstance().getEnvVarName("cache"));
    }

    @Test
    public void testCryptoCommandEncryptsDirectly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        
        CLI cliObj = new CLI(null, in, out, null, false, false);

        CLIContext cli = CLIContext.builder()
                .cli(cliObj)
                .out(out)
                .build();

        CRYPTO cmd = new CRYPTO();
        
        // args[0] = "crypto", args[1] = "my-secret-password"
        cmd.exec(cli, new String[]{"crypto", "my-secret-password"});
        
        String output = out.toString().trim();
        assertNotNull(output);
        assertTrue(output.startsWith("enc::"), "Output should start with 'enc::'");
        
        // Let's decrypt it to make sure it encrypted correctly
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        String decrypted = provider.get(output.substring(5));
        assertEquals("my-secret-password", decrypted, "Decrypted text should match the CLI input");
    }

    @Test
    public void testCryptoCommandWithKeyName() throws Exception {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("db"), SystemKeyManager.getInstance().generateKey("db"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        
        CLI cliObj = new CLI(null, in, out, null, false, false);

        CLIContext cli = CLIContext.builder()
                .cli(cliObj)
                .out(out)
                .build();

        CRYPTO cmd = new CRYPTO();
        
        // args[0] = "crypto", args[1] = "my-secret-password", args[2] = "db"
        cmd.exec(cli, new String[]{"crypto", "my-secret-password", "db"});
        
        String output = out.toString().trim();
        assertNotNull(output);
        assertTrue(output.startsWith("enc::db:"), "Output should start with 'enc::db:'");
        
        // Decrypt to verify
        CryptoEnvironmentProvider provider = new CryptoEnvironmentProvider();
        String decrypted = provider.get(output.substring(5));
        assertEquals("my-secret-password", decrypted, "Decrypted text should match the CLI input");
    }

    @Test
    public void testCryptoCommandTooManyArguments() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        
        CLI cliObj = new CLI(null, in, out, null, false, false);

        CLIContext cli = CLIContext.builder()
                .cli(cliObj)
                .out(out)
                .build();

        CRYPTO cmd = new CRYPTO();
        
        // passing 4 arguments: args[0]="crypto", args[1]="secret", args[2]="db", args[3]="extra"
        cmd.exec(cli, new String[]{"crypto", "my-secret-password", "db", "extra"});
        
        String output = out.toString().trim();
        assertTrue(output.contains("Usage: crypto"), "Output should contain usage instructions when given too many arguments");
    }

    @Test
    public void testCryptoDirectly() throws Exception {
        String input = "my-password";
        String encrypted = CryptoEnvironmentProvider.encrypt(input);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::"), "Output should start with 'enc::'");

        String encryptedPart = encrypted.substring(5);

        byte[] decoded = java.util.Base64.getDecoder().decode(encryptedPart);
        assertNotNull(decoded);
        assertTrue(decoded.length > 12);
    }

    @Test
    public void testDifferentEncryptions() throws Exception {
        String input = "same-password";
        String encrypted1 = CryptoEnvironmentProvider.encrypt(input);
        String encrypted2 = CryptoEnvironmentProvider.encrypt(input);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    public void testEncryptWithKeyName() throws Exception {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("db"), SystemKeyManager.getInstance().generateKey("db"));

        String input = "my-password";
        String encrypted = CryptoEnvironmentProvider.encrypt(input, "db");

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("enc::db:"), "Output should start with 'enc::db:'");
    }

    @Test
    public void testEncryptWithMultipleKeyNames() throws Exception {
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("db"), SystemKeyManager.getInstance().generateKey("db"));
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("api"), SystemKeyManager.getInstance().generateKey("api"));
        System.setProperty(SystemKeyManager.getInstance().getEnvVarName("cache"), SystemKeyManager.getInstance().generateKey("cache"));

        String input = "test-password";
        String encryptedDb = CryptoEnvironmentProvider.encrypt(input, "db");
        String encryptedApi = CryptoEnvironmentProvider.encrypt(input, "api");
        String encryptedCache = CryptoEnvironmentProvider.encrypt(input, "cache");

        assertNotNull(encryptedDb);
        assertNotNull(encryptedApi);
        assertNotNull(encryptedCache);

        assertTrue(encryptedDb.startsWith("enc::db:"));
        assertTrue(encryptedApi.startsWith("enc::api:"));
        assertTrue(encryptedCache.startsWith("enc::cache:"));

        String base64Db = encryptedDb.substring(7);
        String base64Api = encryptedApi.substring(8);
        String base64Cache = encryptedCache.substring(9);

        assertNotEquals(base64Db, base64Api, "Base64 parts should be different for different keys");
        assertNotEquals(base64Api, base64Cache, "Base64 parts should be different for different keys");
        assertNotEquals(base64Db, base64Cache, "Base64 parts should be different for different keys");
    }

}
