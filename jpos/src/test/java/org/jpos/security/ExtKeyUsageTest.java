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

package org.jpos.security;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ExtKeyUsageTest {

    private static final String TEST_EXTERNAL_KEYUSAGES = "META-INF/org/jpos/security/proprietary-invalid.properties";


    @Test
    public void testValueOfByCodeTEK() {
        KeyUsage ret = ExtKeyUsage.valueOfByCode("32");
        assertEquals("32", ret.getCode());
        assertEquals("Terminal Encryption Key", ret.getName());
        assertSame(ExtKeyUsage.TEK, ret);
    }

    @Test
    public void testValueOfByCodeZPK() {
        KeyUsage ret = ExtKeyUsage.valueOfByCode("27");
        assertEquals("27", ret.getCode());
        assertEquals("Zone PIN Encryption Key", ret.getName());
        assertSame(ExtKeyUsage.ZPK, ret);
    }

    @Test
    public void testValueOfByCodeKEK() {
        KeyUsage ret = ExtKeyUsage.valueOfByCode("K0");
        assertEquals("K0", ret.getCode());
        assertSame(KeyUsage.KEK, ret);
    }

    @Test
    public void testEntries() {
        Map<String, KeyUsage> ret = ExtKeyUsage.entries();
        assertTrue(ret.containsKey("D0"));
        assertTrue(ret.containsKey("K0"));
        assertTrue(ret.containsKey("M0"));
        assertTrue(ret.containsKey("V0"));
        assertTrue(ret.containsKey("12"));
        assertTrue(ret.containsKey("15"));
        assertTrue(ret.containsKey("17"));
    }

    @Test
    public void testLoadPropertiesFromClasspath() {
        ExtKeyUsage.loadKeyUsagesFromClasspath(TEST_EXTERNAL_KEYUSAGES);
    }

}
