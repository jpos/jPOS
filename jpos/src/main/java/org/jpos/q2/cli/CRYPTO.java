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

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jpos.core.SystemKeyManager;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

/**
 * Encrypt a secret using AES-256-GCM
 * <p>
 * Usage: crypto "secret"
 * Output: enc::<base64-encoded-ciphertext>
 */
public class CRYPTO implements CLICommand {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void exec(CLIContext cli, String[] args) throws Exception {
        if (args.length < 2 || args.length > 4) {
            usage(cli);
            return;
        }

        String command = args[1];

        if ("generate".equals(command)) {
            handleGenerate(cli, args);
        } else {
            handleEncrypt(cli, command, args);
        }
    }

    private void handleGenerate(CLIContext cli, String[] args) {
        String keyName = args.length > 2 ? args[2] : null;
        SystemKeyManager manager = SystemKeyManager.getInstance();

        String envVarName = manager.generateKey(keyName);
        String keyBase64 = manager.getKeyBase64(keyName);

        cli.println("=== Key Generated ===");
        cli.println("Key Name: " + (keyName == null ? "default" : keyName));
        cli.println("Environment Variable: " + envVarName);
        cli.println("Key (Base64): " + keyBase64);
        cli.println("=====================");
    }

    private void handleEncrypt(CLIContext cli, String command, String[] args) {
        String value = command.startsWith("crypto::") ? command.substring(8) : args[1];
        String keyName = args.length > 2 ? args[2] : null;

        String encrypted = encrypt(value, keyName);
        cli.println(encrypted);
    }

    public void usage(CLIContext cli) {
        cli.println("Usage: crypto \"secret\" [keyName]");
        cli.println("Encrypts a secret using AES-256-GCM authenticated encryption.");
        cli.println("Output format: enc::keyname::<base64-encoded-ciphertext>");
        cli.println("If keyName is not provided, uses the default key.");
        cli.println("The encrypted value can be used in db.properties with the enc: prefix.");
    }

    public String encrypt(String value) {
        return encrypt(value, null);
    }

    public String encrypt(String value, String keyName) {
        try {
            // Use SystemKeyManager to get the key, generating it if it doesn't exist
            SecretKey key = SystemKeyManager.getInstance().getKey(keyName);
            if (key == null) {
                // Key doesn't exist, generate it
                SystemKeyManager.getInstance().generateKey(keyName);
                key = SystemKeyManager.getInstance().getKey(keyName);
            }
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), ALGORITHM);

            // Generate secure random 12-byte IV
            byte[] iv = new byte[IV_SIZE_BYTES];
            secureRandom.nextBytes(iv);

            // Encrypt with GCM authentication
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] ciphertext = cipher.doFinal(value.getBytes());

            // Combine IV and ciphertext
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);

            String base64 = Base64.getEncoder().encodeToString(buf.array());

            // If keyName is provided, include it in the prefix
            if (keyName != null && !keyName.isEmpty()) {
                return "enc::" + keyName + "::" + base64;
            } else {
                return "enc::" + base64;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt value", e);
        }
    }
}