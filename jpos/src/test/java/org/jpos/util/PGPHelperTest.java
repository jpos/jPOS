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

package org.jpos.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PGPHelperTest {
    @Test
    public void testGetVerifiedLicenseText() throws Exception {
        String licenseText = PGPHelper.getVerifiedLicenseText();

        assertNotNull(licenseText);
        assertTrue(licenseText.contains("jPOS Community Edition"));
        assertFalse(licenseText.contains("BEGIN PGP SIGNATURE"));
    }

    @Test
    public void testGetVerifiedLicenseTextRejectsTamperedLicense() throws Exception {
        String previousLicensee = System.getProperty("LICENSEE");
        Path license = Path.of("src/main/resources/LICENSEE.asc");
        Path tamperedLicense = Files.createTempFile("licensee", ".asc");
        Files.writeString(
          tamperedLicense,
          Files.readString(license, StandardCharsets.UTF_8).replace("jPOS Community Edition", "jPOS Tampered Edition"),
          StandardCharsets.UTF_8
        );

        try {
            System.setProperty("LICENSEE", tamperedLicense.toString());
            assertNull(PGPHelper.getVerifiedLicenseText());
        } finally {
            if (previousLicensee != null)
                System.setProperty("LICENSEE", previousLicensee);
            else
                System.clearProperty("LICENSEE");
            Files.deleteIfExists(tamperedLicense);
        }
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        String s = "The quick brown fox jumps over the lazy dog 0123456789";
        byte[] cypertext = PGPHelper.encrypt(
          s.getBytes(StandardCharsets.UTF_8),
          "src/dist/cfg/demo.pub",
          "abc.txt", true, true, "demo@jpos.org");

        byte[] clearText = PGPHelper.decrypt(cypertext, "src/dist/cfg/demo.priv", "demo".toCharArray());
        assertEquals(s, new String(clearText, StandardCharsets.UTF_8));
    }
}
