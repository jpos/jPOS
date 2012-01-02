/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class ProfilerTest {

    @Test
    public void testCheckPoint() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.checkPoint("testProfilerDetail1");

    }

    @Test
    public void testCheckPointNull() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.checkPoint(null);
        assertEquals("profiler.events.size()", 1, profiler.events.size());

    }

    @Test
    public void testConstructor() throws Throwable {
        Profiler profiler = new Profiler();
        assertEquals("profiler.events.size()", 0, profiler.events.size());
    }

    @Test
    public void testDump() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.dump(new PrintStream(new ByteArrayOutputStream()), "testProfilerIndent");
        assertEquals("profiler.events.size()", 1, profiler.events.size());
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        Profiler profiler = new Profiler();
        try {
            profiler.dump(null, "testProfilerIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("profiler.events.size()", 1, profiler.events.size());
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetPartial() throws Throwable {
        new Profiler().getPartial();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testReset() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.reset();
        assertEquals("profiler.events.size()", 0, profiler.events.size());
    }
}
