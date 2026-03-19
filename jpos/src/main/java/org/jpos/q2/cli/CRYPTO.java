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

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encrypt a secret using AES-256-GCM
 * <p>
 * Usage: crypto "secret"
 * Output: crypto::<base64-encoded-ciphertext>
 */
public class CRYPTO implements CLICommand {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    @Override
    public void exec(CLIContext cli, String[] args) throws Exception {
        if (args.length != 2) {
            usage(cli);
            return;
        }
        cli.println(String.format("crypto::%s", encrypt(args[1])));
    }

    public void usage(CLIContext cli) {
        cli.println("Usage: crypto \"secret\"");
        cli.println("Encrypts a secret using AES-256-GCM authenticated encryption.");
        cli.println("Output format: crypto::<base64-encoded-ciphertext>");
        cli.println("The encrypted value can be used in db.properties with the crypto: prefix.");
    }

    public String encrypt(String value) {
        try {
            // Use first 32 bytes of SystemSeed as the 256-bit AES key
            byte[] seed = org.jpos.security.SystemSeed.getSeed(0, 32);
            SecretKeySpec keySpec = new SecretKeySpec(seed, ALGORITHM);

            // Generate secure random 12-byte IV
            byte[] iv = new byte[IV_SIZE_BYTES];
            new SecureRandom().nextBytes(iv);

            // Encrypt with GCM authentication
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);

            return "crypto::" + Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt value", e);
        }
    }
}