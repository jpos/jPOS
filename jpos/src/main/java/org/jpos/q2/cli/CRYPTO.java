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
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

/**
 * Encrypt a secret using AES-256-GCM
 * <p>
 * Usage: crypto "secret"
 * Output: enc::base64-encoded-ciphertext
 */
public class CRYPTO implements CLICommand {

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

        String keyBase64 = manager.generateKey(keyName);
        String envVarName = manager.getEnvVarName(keyName);

        cli.println("=== Key Generated ===");
        cli.println("Key Name: " + (keyName == null ? "default" : keyName));
        cli.println("Environment Variable: " + envVarName);
        cli.println("Key (Base64): " + keyBase64);
        cli.println("=====================");
    }

    private void handleEncrypt(CLIContext cli, String command, String[] args) {
        String value = command.startsWith("crypto::") ? command.substring(8) : args[1];
        String keyName = args.length > 2 ? args[2] : null;

        String encrypted = CryptoEnvironmentProvider.encrypt(value, keyName);
        cli.println(encrypted);
    }

    public void usage(CLIContext cli) {
        cli.println("Usage: crypto \"secret\" [keyName]");
        cli.println("Encrypts a secret using AES-256-GCM authenticated encryption.");
        cli.println("Output format: enc::keyname:base64-encoded-ciphertext");
        cli.println("If keyName is not provided, uses the default key.");
        cli.println("The encrypted value can be used in db.properties with the enc: prefix.");
    }
}
