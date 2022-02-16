/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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

package org.jpos.core;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnvironmentTest {
    static String oldEnv;
    static String oldEnvdir;
    static final String TEST_ENVDIR = "build/resources/test/org/jpos/core/";

    @BeforeAll
    public static void setUp() throws IOException
    {
        oldEnv = System.getProperty("jpos.env");            // save it to restore it later
        oldEnvdir = System.getProperty("jpos.envdir");      // save it to restore it later

        System.setProperty("jpos.env", "testenv");
        System.setProperty("jpos.envdir", TEST_ENVDIR);
        Environment.reload();                               // reload new env from new dir
    }

    @AfterAll
    public static void tearDown() throws Exception {
        // restore old env and envdir
        if (oldEnv != null)
            System.setProperty("jpos.env", oldEnv);
        else
            System.clearProperty("jpos.env");

        if (oldEnvdir != null)
            System.setProperty("jpos.envdir", oldEnvdir);
        else
            System.clearProperty("jpos.envdir");

        Environment.reload();
    }


    // This test only uses System properties to try to override the values in the env file.
    // A more complete version would also set an OS environment variable, which can't be
    // easily done from Java.
    // Changing env vars for tests can be done by using this JUnit extension.
    //      https://junit-pioneer.org/
    @Test
    public void testFromCfg() {
        System.setProperty("test.value", "from sys prop");
        assertEquals("from testenv.yml", Environment.get("$cfg{test.value}"));
        assertEquals("from testenv.yml", System.getProperty("test.sys"));
    }


    @Test
    public void testEmptyDefault() {
        assertEquals("", Environment.get("${test:}"));
    }

    @Test
    public void testObfuscated() {
        System.setProperty("obf.value", "obf::D4sCOgAAAASneiqWUPCruOtNmAU78cg6uBAv3N0/8DSNK6ptaozLAg==");
        assertEquals("OBFUSCATED ABCD", Environment.get("OBFUSCATED ${obf.value}"));
    }

    @Test
    public void testLoop() {
        System.setProperty("loop", "${loop}");
        assertEquals("${loop}", Environment.get("${loop}"));
    }
}
