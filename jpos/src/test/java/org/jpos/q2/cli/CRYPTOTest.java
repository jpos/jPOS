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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for CRYPTO command
 */
public class CRYPTOTest {

    @Test
    public void testCryptoDirectly() throws Exception {
        CRYPTO crypto = new CRYPTO();

        // Test encryption directly
        String input = "my-password";
        String encrypted = crypto.encrypt(input);

        System.out.println("Encrypted: " + encrypted);

        // Verify it's valid output
        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("crypto:"), "Output should start with 'crypto:'");

        String encryptedPart = encrypted.substring(8);

        // Verify it's valid base64
        byte[] decoded = java.util.Base64.getDecoder().decode(encryptedPart);
        assertNotNull(decoded);
        assertTrue(decoded.length > 12); // Should have IV + ciphertext
    }

    @Test
    public void testDifferentEncryptions() throws Exception {
        CRYPTO crypto = new CRYPTO();

        // Each encryption should produce different output (due to random IV)
        String input = "same-password";
        String encrypted1 = crypto.encrypt(input);
        String encrypted2 = crypto.encrypt(input);

        assertNotEquals(encrypted1, encrypted2);
    }
}