/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PGPHelperTest {
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
