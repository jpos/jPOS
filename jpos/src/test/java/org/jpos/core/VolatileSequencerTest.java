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

package org.jpos.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// import junitx.util.PrivateAccessor;

import org.junit.jupiter.api.Test;

public class VolatileSequencerTest {

//    @Test
//    public void testConstructor() throws Throwable {
//        VolatileSequencer volatileSequencer = new VolatileSequencer();
//        assertNotNull(PrivateAccessor.getField(volatileSequencer, "map"));
//    }

    @Test
    public void testGet() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        volatileSequencer.get("abcdefghijklmnopqrstuvwxyz", 1);
        int result = volatileSequencer.get("abcdefghijklmnopqrstuvwxyz", -1);
        assertEquals(0, result, "result");
    }

    @Test
    public void testGet1() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int add = volatileSequencer.get("  ", -1);
        int result = volatileSequencer.get("  ", 0);
        assertEquals(add, result, "result");
    }

    @Test
    public void testGet2() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int result = volatileSequencer.get("testVolatileSequencerCounterName", 0);
        assertEquals(0, result, "result");
    }

    @Test
    public void testGet3() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int result = volatileSequencer.get("testVolatileSequencerCounterName");
        assertEquals(1, result, "result");
    }

    @Test
    public void testGet4() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        volatileSequencer.set("testString", -2);
        volatileSequencer.get("testString");
        int result = volatileSequencer.get("testString");
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetCounterNames() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        volatileSequencer.get("testVolatileSequencerCounterName", 100);
        String[] result = volatileSequencer.getCounterNames();
        assertEquals(1, result.length, "result.length");
        assertEquals("testVolatileSequencerCounterName", result[0], "result[0]");
    }

    @Test
    public void testGetCounterNames1() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        String[] result = volatileSequencer.getCounterNames();
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testSet() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int add = volatileSequencer.get("", 100);
        int result = volatileSequencer.set("", add);
        assertEquals(add, result, "result");
    }

    @Test
    public void testSet1() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int add = volatileSequencer.get("testString", 0);
        int result = volatileSequencer.set("testString", 100);
        assertEquals(add, result, "result");
    }

    @Test
    public void testSet2() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int result = volatileSequencer.set("testVolatileSequencerCounterName", 100);
        assertEquals(0, result, "result");
    }
}
